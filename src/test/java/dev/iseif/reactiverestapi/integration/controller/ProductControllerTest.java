package dev.iseif.reactiverestapi.integration.controller;

import dev.iseif.reactiverestapi.model.Product;
import dev.iseif.reactiverestapi.repository.ProductRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ProductControllerTest {

  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private ProductRepository productRepository;
  private WebTestClient client;
  private List<Product> expectedProducts;

  @BeforeEach
  void setUp() {
    client = WebTestClient
        .bindToApplicationContext(applicationContext)
        .configureClient()
        .baseUrl("/api/products")
        .build();

    Flux<Product> initData = productRepository.deleteAll()
        .thenMany(Flux.just(
          Product.builder().title("Apple iPhone XS Max").description("New iPhone XS Max").price(1099.99).build(),
          Product.builder().title("Apple MacBook Pro").description("New MacBook").price(2599.99).build(),
          Product.builder().title("Samsung Galaxy S10+").description("New Galaxy!!").price(799.99).build())
        .flatMap(productRepository::save))
        .thenMany(productRepository.findAll());

    expectedProducts = initData.collectList().block();
  }

  @Test
  void getAllProducts() {
    client.get().uri("/").exchange()
        .expectStatus().isOk()
        .expectBodyList(Product.class).isEqualTo(expectedProducts);
  }

  @Test
  void getProductById_whenProductExists_returnCorrectProduct() {
    Product expectedProduct = expectedProducts.get(0);

    client.get().uri("/{id}", expectedProduct.getId()).exchange()
        .expectStatus().isOk()
        .expectBody(Product.class).isEqualTo(expectedProduct);
  }

  @Test
  void getProductById_whenProductNotExist_returnNotFound() {
    String id = "NOT_EXIST_ID";

    client.get().uri("/{id}", id).exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void searchByTitle() {
    String title = "apple";
    List<Product> expectedFilteredProducts = Arrays.asList(expectedProducts.get(0), expectedProducts.get(1));

    client.get().uri("/search/{title}", title).exchange()
        .expectStatus().isOk()
        .expectBodyList(Product.class).isEqualTo(expectedFilteredProducts);
  }

  @Test
  void addProduct() {
    Product expectedProduct = expectedProducts.get(0);

    client.post().uri("/").body(Mono.just(expectedProduct), Product.class).exchange()
        .expectStatus().isCreated()
        .expectBody(Product.class).isEqualTo(expectedProduct);
  }

  @Test
  void addProduct_whenProductIsInvalid_returnBadRequest() {
    Product product = Product.builder().title("title").description("").price(0.0).build();

    client.post().uri("/").body(Mono.just(product), Product.class).exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void updateProduct_whenProductExists_performUpdate() {
    Product expectedProduct = expectedProducts.get(0);

    client.put().uri("/{id}", expectedProduct.getId()).body(Mono.just(expectedProduct), Product.class).exchange()
        .expectStatus().isOk()
        .expectBody(Product.class).isEqualTo(expectedProduct);
  }

  @Test
  void updateProduct_whenProductNotExist_returnNotFound() {
    String id = "NOT_EXIST_ID";
    Product expectedProduct = expectedProducts.get(0);

    client.put().uri("/{id}", id).body(Mono.just(expectedProduct), Product.class).exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void deleteProduct_whenProductExists_performDeletion() {
    Product productToDelete = expectedProducts.get(0);

    client.delete().uri("/{id}", productToDelete.getId()).exchange()
        .expectStatus().isOk();
  }

  @Test
  void deleteProduct_whenIdNotExist_returnNotFound() {
    String id = "NOT_EXIST_ID";

    client.delete().uri("/{id}", id).exchange()
        .expectStatus().isNotFound();
  }
}
