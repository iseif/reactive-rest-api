package dev.iseif.reactiverestapi.integration.service;

import dev.iseif.reactiverestapi.model.Product;
import dev.iseif.reactiverestapi.repository.ProductRepository;
import dev.iseif.reactiverestapi.service.ProductService;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@DataMongoTest
@Import(ProductService.class)
class ProductServiceTest {

  private final Product product1 = Product.builder().title("Apple iPhone XS Max").description("New iPhone XS Max").price(1099.99).build();
  private final Product product2 = Product.builder().title("Apple MacBook Pro").description("New MacBook").price(2599.99).build();
  private final Product product3 = Product.builder().title("Samsung Galaxy S10+").description("New Galaxy!!").price(799.99).build();

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private ProductService productService;

  private List<Product> allProducts;

  @BeforeEach
  void setUp() {
    Flux<Product> initData = productRepository.deleteAll().thenMany(
        Flux.just(product1, product2, product3)
            .flatMap(productRepository::save)).thenMany(productRepository.findAll());

    allProducts = initData.collectList().block();
  }

  @Test
  void getAll() {
    Flux<Product> actual = productService.getAll();

    StepVerifier
        .create(actual)
        .expectNextMatches(allProducts::contains)
        .expectNextMatches(allProducts::contains)
        .expectNextMatches(allProducts::contains)
        .verifyComplete();
  }

  @Test
  void getById() {
    Product expectedProduct = allProducts.get(0);

    Mono<Product> actual = productService.getById(expectedProduct.getId());

    StepVerifier
        .create(actual)
        .expectNext(expectedProduct)
        .verifyComplete();
  }

  @Test
  void searchByTitle() {
    final String title = "apple";
    List<Product> expectedProducts = Arrays.asList(product1, product2);

    Flux<Product> actual = productService.searchByTitle(title);

    StepVerifier
        .create(actual)
        .expectNextMatches(expectedProducts::contains)
        .expectNextMatches(expectedProducts::contains)
        .verifyComplete();
  }

  @Test
  void create() {
    Product product = Product.builder().title("New Test Product").description("Test Product").price(299.99).build();

    Mono<Product> actual = productService.create(product);

    StepVerifier
        .create(actual)
        .expectNextMatches(actualProduct -> actualProduct.equals(product) && StringUtils.isNotBlank(actualProduct.getId()))
        .verifyComplete();
  }

  @Test
  void update() {
    Product productToUpdate = allProducts.get(0);
    Product updatedProduct = Product.builder().title("New Updated Title").description("Updated").price(299.99).build();

    Mono<Product> actual = productService.update(productToUpdate.getId(), updatedProduct)
        .flatMap(product -> productRepository.findById(productToUpdate.getId()));

    StepVerifier
        .create(actual)
        .expectNextMatches(actualProduct -> actualProduct.equals(updatedProduct))
        .verifyComplete();
  }

  @Test
  void deleteById() {
    Product productToDelete = allProducts.get(0);

    Mono<Product> actual = productService.deleteById(productToDelete.getId())
        .flatMap(product -> productRepository.findById(productToDelete.getId()));

    StepVerifier
        .create(actual)
        .verifyComplete();
  }
}
