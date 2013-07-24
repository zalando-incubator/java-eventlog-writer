package de.zalando.jpa.example.sequences;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author  jbellmann
 */
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> { }
