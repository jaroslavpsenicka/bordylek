package org.bordylek.mon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ConfigurationService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private Config config;

    private static final String DOCUMENT_ID = "1";

    public Set<String> getDisabledRules() {
        return config().getDisabledRules();
    }

    public void enableRule(String fqRuleName) {
        config().getDisabledRules().remove(fqRuleName);
    }

    public void disableRule(String fqRuleName) {
        config().getDisabledRules().add(fqRuleName);
    }

    private Config config() {
        if (config == null) {
            BasicQuery query = new BasicQuery("{ id: '" + DOCUMENT_ID + "' }");
            config = mongoTemplate.findOne(query, Config.class, "metrics");
            if (config == null) {
                config = new Config();
                config.setId(DOCUMENT_ID);
                config.setDisabledRules(new HashSet<String>());
                mongoTemplate.save(config);
            }
        }

        return config;
    }

    public static class Config {

        private String id;
        private Set<String> disabledRules;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Set<String> getDisabledRules() {
            return disabledRules;
        }

        public void setDisabledRules(Set<String> disabledRules) {
            this.disabledRules = disabledRules;
        }
    }

}
