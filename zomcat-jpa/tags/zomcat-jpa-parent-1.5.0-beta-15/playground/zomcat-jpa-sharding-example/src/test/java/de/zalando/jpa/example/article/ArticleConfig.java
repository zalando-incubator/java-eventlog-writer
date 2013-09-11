package de.zalando.jpa.example.article;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Partitioned;

import de.zalando.sprocwrapper.sharding.ShardedObject;

@Entity
@IdClass(ArticleSkuPk.class)
@Table(name = "article_config", schema = "zzj_data")
@Partitioned(ArticlePartitions.SHARDED_OBJECT_PARTITIONING)
public class ArticleConfig implements ShardedObject {

    @Id
    @OneToOne
    private ArticleSku articlesku;

    private String name;

// @ManyToOne
// private ArticleModel articleModel;

    protected ArticleConfig() {
        // just for JPA
    }

    public ArticleConfig(final ArticleSku articlesku) {
        this.articlesku = articlesku;
    }

// public ArticleModel getArticleModel() {
// return articleModel;
// }
//
// public void setArticleModel(final ArticleModel articleModel) {
// this.articleModel = articleModel;
// }

    public ArticleSku getConfigSku() {
        return articlesku;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Object getShardKey() {
        return this.articlesku.getShardKey();
    }

}