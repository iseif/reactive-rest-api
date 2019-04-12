package dev.iseif.reactiverestapi.controller;

import static org.mockito.Mockito.when;

import dev.iseif.reactiverestapi.model.Product;
import dev.iseif.reactiverestapi.service.ProductService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
class ProductControllerTest {

  @MockBean
  private ProductService productService;
  private WebTestClient client;
  private List<Product> expectedProducts;

  @BeforeEach
  void setUp() {
    client = WebTestClient
        .bindToController(new ProductController(productService))
        .configureClient()
        .baseUrl("/api/products")
        .build();

    expectedProducts = Arrays.asList(
        Product.builder().id("1").title("Apple iPhone XS Max").description("New iPhone XS Max").price(1099.99).build(),
        Product.builder().id("2").title("Apple MacBook Pro").description("New MacBook").price(2599.99).build(),
        Product.builder().id("3").title("Samsung Galaxy S10+").description("New Galaxy!!").price(799.99).build());
  }

  @Test
  void getAllProducts() {
    when(productService.getAll()).thenReturn(Flux.fromIterable(expectedProducts));

    client.get().uri("/").exchange()
        .expectStatus().isOk()
        .expectBodyList(Product.class).isEqualTo(expectedProducts);
  }

  @Test
  void getProductById_whenProductExists_returnCorrectProduct() {
    Product expectedProduct = expectedProducts.get(0);
    when(productService.getById(expectedProduct.getId())).thenReturn(Mono.just(expectedProduct));

    client.get().uri("/{id}", expectedProduct.getId()).exchange()
        .expectStatus().isOk()
        .expectBody(Product.class).isEqualTo(expectedProduct);
  }

  @Test
  void getProductById_whenProductNotExist_returnNotFound() {
    String id = "NOT_EXIST_ID";
    when(productService.getById(id)).thenReturn(Mono.empty());

    client.get().uri("/{id}", id).exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void searchByTitle() {
    String title = "apple";
    List<Product> expectedFilteredProducts = Arrays.asList(expectedProducts.get(0), expectedProducts.get(1));
    when(productService.searchByTitle(title)).thenReturn(Flux.fromIterable(expectedFilteredProducts));

    client.get().uri("/search/{title}", title).exchange()
        .expectStatus().isOk()
        .expectBodyList(Product.class).isEqualTo(expectedFilteredProducts);
  }

  @Test
  void addProduct() {
    Product expectedProduct = expectedProducts.get(0);
    when(productService.create(expectedProduct)).thenReturn(Mono.just(expectedProduct));

    client.post().uri("/").body(Mono.just(expectedProduct), Product.class).exchange()
        .expectStatus().isCreated()
        .expectBody(Product.class).isEqualTo(expectedProduct);
  }

  @Test
  void updateProduct_whenProductExists_performUpdate() {
    Product expectedProduct = expectedProducts.get(0);
    when(productService.update(expectedProduct.getId(), expectedProduct)).thenReturn(Mono.just(expectedProduct));

    client.put().uri("/{id}", expectedProduct.getId()).body(Mono.just(expectedProduct), Product.class).exchange()
        .expectStatus().isOk()
        .expectBody(Product.class).isEqualTo(expectedProduct);
  }

  @Test
  void updateProduct_whenProductNotExist_returnNotFound() {
    String id = "NOT_EXIST_ID";
    Product expectedProduct = expectedProducts.get(0);
    when(productService.update(id, expectedProduct)).thenReturn(Mono.empty());

    client.put().uri("/{id}", id).body(Mono.just(expectedProduct), Product.class).exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void deleteProduct_whenProductExists_performDeletion() {
    Product productToDelete = expectedProducts.get(0);
    when(productService.deleteById(productToDelete.getId())).thenReturn(Mono.just(productToDelete));

    client.delete().uri("/{id}", productToDelete.getId()).exchange()
        .expectStatus().isOk();
  }

  @Test
  void deleteProduct_whenIdNotExist_returnNotFound() {
    Product productToDelete = expectedProducts.get(0);
    when(productService.deleteById(productToDelete.getId())).thenReturn(Mono.empty());

    client.delete().uri("/{id}", productToDelete.getId()).exchange()
        .expectStatus().isNotFound();
  }
}
