package de.zalando.jpa.example.order;

import org.junit.Assert;

import org.junit.runner.RunWith;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import org.springframework.data.jpa.auditing.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.transaction.annotation.Transactional;

import de.zalando.jpa.config.JpaConfig;

/**
 * The testcode for integration and unit-test.
 *
 * @author  jbellmann
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
public abstract class AbstractPurchaseOrderTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPurchaseOrderTestSupport.class);

    public static final String packagesToScan = "de.zalando.jpa.example.order";

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    public void doTestSavePurchaseOrder() {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderStatus(OrderStatus.ORDERED);
        order.setBrandCode("BRANDCODE_A");
        purchaseOrderRepository.saveAndFlush(order);

        Assert.assertNotNull(order.getBusinessKey());
        Assert.assertNotNull(order.getBrandCode());
        Assert.assertNotNull(order.getCreatedBy());
        Assert.assertNotNull(order.getModifiedBy());
        Assert.assertNotNull(order.getCreationDate());
        Assert.assertNotNull(order.getModificationDate());

        LOG.info("PurchaseOrder to save on commit : {}", order);

        order.getPositions().add(new PurchaseOrderPosition(order));
        order.getPositions().add(new PurchaseOrderPosition(order));

        order.setAddress(new Address());

        purchaseOrderRepository.saveAndFlush(order);
        LOG.info("PurchaseOrder to save with Postions : {}", order);

        for (PurchaseOrderPosition pos : order.getPositions()) {
            LOG.info("Saved Position : {}", pos.toString());
        }

        LOG.info("PurchaseOrder to save with Postions : {}", order);
        LOG.info("Address : {}", order.getAddress());

        LOG.info("---- NOW DELETE THE FIRST ----------");
        order.getPositions().remove(0);
        purchaseOrderRepository.saveAndFlush(order);

        LOG.info("---- AFTER DELETE ----------");
        for (PurchaseOrderPosition pos : order.getPositions()) {
            LOG.info("Saved Position : {}", pos.toString());
        }

        LOG.info("PurchaseOrder to save with Postions : {}", order);
        LOG.info("Address : {}", order.getAddress());

    }

    @Configuration
    @EnableJpaRepositories(AbstractPurchaseOrderTestSupport.packagesToScan)
    @EnableJpaAuditing
    @Import({ JpaConfig.class })
    @ImportResource({ "classpath:/enableAuditing.xml" })
    static class TestConfig { }
}
