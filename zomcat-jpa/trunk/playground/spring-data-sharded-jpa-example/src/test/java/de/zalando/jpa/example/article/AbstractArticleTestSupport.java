package de.zalando.jpa.example.article;

import org.junit.Assert;

import org.junit.runner.RunWith;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.transaction.annotation.Transactional;

import de.zalando.jpa.config.JpaConfig;
import de.zalando.jpa.config.PersistenceUnitNameProvider;
import de.zalando.jpa.config.StandardPersistenceUnitNameProvider;

/**
 * The testcode for integration and unit-test.
 *
 * @author  jbellmann
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Transactional
public abstract class AbstractArticleTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractArticleTestSupport.class);

    public static final String PACKAGES_TO_SCAN = "de.zalando.jpa.example.article";
    public static final String PU_NAME = "article";

    @Autowired
    private ArticleModelRepository articleModelRepository;

    @Autowired
    private ArticleSkuRepository articleSkuRepository;

    public void doTestSaveArticleModel() {
        Assert.assertNotNull(articleModelRepository);

        ArticleSku id = new ArticleSku();
        id.setSkuType(SkuType.CONFIG);
        id = this.articleSkuRepository.saveAndFlush(id);

        ArticleModel articleModel = new ArticleModel(id);
        articleModel = articleModelRepository.saveAndFlush(articleModel);
    }

    @Configuration
    @EnableJpaRepositories(AbstractArticleTestSupport.PACKAGES_TO_SCAN)
    @Import({ JpaConfig.class })
    static class TestConfig {

        @Bean
        public PersistenceUnitNameProvider persistenceUnitNameProvider() {
            return new StandardPersistenceUnitNameProvider(PU_NAME);
        }
    }
}
