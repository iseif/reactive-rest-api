package dev.iseif.reactiverestapi.service;

import static org.mockito.Mockito.when;

import dev.iseif.reactiverestapi.model.Product;
import dev.iseif.reactiverestapi.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ProductServiceTest {

  private final Product product1 = Product.builder().title("Apple iPhone XS Max").description("New iPhone XS Max").price(1099.99).build();
  private final Product product2 = Product.builder().title("Apple MacBook Pro").description("New MacBook").price(2599.99).build();
  private final Product product3 = Product.builder().title("Samsung Galaxy S10+").description("New Galaxy!!").price(799.99).build();

  @MockBean
  private ProductRepository productRepository;

  @Autowired
  private ProductService productService;

  @Test
  void getAll() {
    when(productRepository.findAll()).thenReturn(Flux.just(product1, product2, product3));

    Flux<Product> actual = productService.getAll();

    assertResults(actual, product1, product2, product3);
  }

  @Test
  void getById_whenIdExists_returnCorrectProduct() {
    when(productRepository.findById(product1.getId())).thenReturn(Mono.just(product1));

    Mono<Product> actual = productService.getById(product1.getId());

    assertResults(actual, product1);
  }

  @Test
  void getById_whenIdNotExist_returnEmptyMono() {
    when(productRepository.findById(product1.getId())).thenReturn(Mono.empty());

    Mono<Product> actual = productService.getById(product1.getId());

    assertResults(actual);
  }

  @Test
  void searchByTitle() {
    final String title = "apple";
    when(productRepository.findByTitleContainingIgnoreCase(title)).thenReturn(Flux.just(product1, product2));

    Flux<Product> actual = productService.searchByTitle(title);

    assertResults(actual, product1, product2);
  }

  @Test
  void create() {
    when(productRepository.save(product1)).thenReturn(Mono.just(product1));

    Mono<Product> actual = productService.create(product1);

    assertResults(actual, product1);
  }

  @Test
  void update_whenIdExists_returnUpdatedProduct() {
    when(productRepository.findById(product1.getId())).thenReturn(Mono.just(product1));
    when(productRepository.save(product1)).thenReturn(Mono.just(product1));

    Mono<Product> actual = productService.update(product1.getId(), product1);

    assertResults(actual, product1);
  }

  @Test
  void update_whenIdNotExist_returnEmptyMono() {
    when(productRepository.findById(product1.getId())).thenReturn(Mono.empty());

    Mono<Product> actual = productService.update(product1.getId(), product1);

    assertResults(actual);
  }

  @Test
  void delete_whenProductExists_performDeletion() {
    when(productRepository.findById(product1.getId())).thenReturn(Mono.just(product1));
    when(productRepository.delete(product1)).thenReturn(Mono.empty());

    Mono<Product> actual = productService.deleteById(product1.getId());

    assertResults(actual, product1);
  }

  @Test
  void delete_whenIdNotExist_returnEmptyMono() {
    when(productRepository.findById(product1.getId())).thenReturn(Mono.empty());

    Mono<Product> actual = productService.deleteById(product1.getId());

    assertResults(actual);
  }

  private void assertResults(Publisher<Product> publisher, Product... expectedProducts) {
    StepVerifier
        .create(publisher)
        .expectNext(expectedProducts)
        .verifyComplete();
  }
}
