package dev.iseif.reactiverestapi.repository;

import dev.iseif.reactiverestapi.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

  Flux<Product> findByTitleContainingIgnoreCase(String title);
}
