package org.bordylek.mon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DataController {

	private MongoTemplate mongoTemplate;

	private static final Logger LOG = LoggerFactory.getLogger(DataController.class);

    @RequestMapping(value = "/data", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<?> getData(@RequestParam(value = "class") String className,
		@RequestParam(value = "queryString", required = false) String queryString,
        @RequestParam(value = "skip", defaultValue = "0") Integer skip,
        @RequestParam(value = "limit", defaultValue = "100") Integer limit) throws ClassNotFoundException {
        Query query = createQuery(queryString).skip(skip).limit(limit);
        return mongoTemplate.find(query, Class.forName(className));
	}

    private Query createQuery(String queryString) {
        Criteria criteria = null;
        for (String condition : queryString.split(",")) {
            if (condition.contains(":")) {
                criteria = createIsCriteria(criteria, condition.split(":"));
            } else if (condition.contains("~")) {
                criteria = createLikeCriteria(criteria, condition.split(">"));
            } else if (condition.contains(">")) {
                criteria = createGtCriteria(criteria, condition.split(">"));
            } else if (condition.contains("<")) {
                criteria = createLtCriteria(criteria, condition.split(">"));
            } else LOG.warn("Error reading query: '" + queryString + "', does not contain any of known operators: :~><");
        }

        return criteria != null ? new Query(criteria) : new Query();
    }

    private Criteria createIsCriteria(Criteria criteria, String[] tokens) {
        if (tokens.length == 2) {
            String key = tokens[0].trim();
            String value = tokens[1].trim();
            return (criteria != null) ? criteria.and(key).is(value) : Criteria.where(key).is(value);
        }

        return criteria;
    }

    private Criteria createLikeCriteria(Criteria criteria, String[] tokens) {
        if (tokens.length == 2) {
            String key = tokens[0].trim();
            String value = tokens[1].trim();
            return (criteria != null) ? criteria.and(key).regex(value) : Criteria.where(key).regex(value);
        }

        return criteria;
    }

    private Criteria createGtCriteria(Criteria criteria, String[] tokens) {
        if (tokens.length == 2) {
            String key = tokens[0].trim();
            String value = tokens[1].trim();
            return (criteria != null) ? criteria.and(key).gt(value) : Criteria.where(key).gt(value);
        }

        return criteria;
    }

    private Criteria createLtCriteria(Criteria criteria, String[] tokens) {
        if (tokens.length == 2) {
            String key = tokens[0].trim();
            String value = tokens[1].trim();
            return (criteria != null) ? criteria.and(key).lt(value) : Criteria.where(key).lt(value);
        }

        return criteria;
    }

}
