package dev.iseif.reactiverestapi.config;

import dev.iseif.reactiverestapi.model.Product;
import dev.iseif.reactiverestapi.repository.ProductRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Log4j2
@Component
@Profile("demo")
public class SampleDataInitializer implements ApplicationListener<ApplicationReadyEvent> {

  private final ProductRepository productRepository;

  public SampleDataInitializer(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    productRepository.deleteAll()
        .thenMany(
            Flux.just(
              Product.builder().title("Apple iPhone XS Max").description("New iPhone XS Max").price(1099.99).build(),
              Product.builder().title("Apple MacBook Pro").description("New MacBook").price(2599.99).build(),
              Product.builder().title("Samsung Galaxy S10+").description("New Galaxy!!").price(799.99).build()
            ).flatMap(productRepository::save))
        .thenMany(productRepository.findAll())
        .subscribe(product -> log.info("Product created:\n" + product.toString()));
  }
}
