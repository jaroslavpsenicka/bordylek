package org.bordylek.mon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;

@RestController
public class DataController {

    @Autowired
	private MongoTemplate mongoTemplate;

    private ObjectMapper objectMapper;

	private static final Logger LOG = LoggerFactory.getLogger(DataController.class);

    public DataController() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(MapperFeature.USE_ANNOTATIONS);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @RequestMapping(value = "/data", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Object getData(@RequestParam(value = "class") String className,
        @RequestParam(value = "queryString", required = false) String queryString,
        @RequestParam(value = "skip", defaultValue = "0") Integer skip,
        @RequestParam(value = "limit", defaultValue = "100") Integer limit) throws ClassNotFoundException, JsonProcessingException {
        Query query = createQuery(queryString).skip(skip).limit(limit);
        return objectMapper.writeValueAsString(mongoTemplate.find(query, Class.forName(className)));
	}

    @RequestMapping(value = "/data/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object getDataById(@PathVariable(value = "id") String id,
        @RequestParam(value = "class") String className) throws ClassNotFoundException, JsonProcessingException {
        return objectMapper.writeValueAsString(mongoTemplate.findById(id, Class.forName(className)));
    }

    @RequestMapping(value = "/data", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void saveData(@RequestBody String body,
        @RequestParam(value = "class") String className) throws ClassNotFoundException, IOException {
        mongoTemplate.save(objectMapper.readValue(body, Class.forName(className)));
    }

    @RequestMapping(value = "/data/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteById(@PathVariable(value = "id") String id,
        @RequestParam(value = "class") String className) throws ClassNotFoundException {
        mongoTemplate.remove(mongoTemplate.findById(id, Class.forName(className)));
    }

    private Query createQuery(String queryString) {
        Criteria criteria = null;
        if (queryString != null && queryString.length() > 0) {
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
        }

        return criteria != null ? new Query(criteria) : new Query();
    }

    private Criteria createIsCriteria(Criteria criteria, String[] tokens) {
        if (tokens.length == 2) {
            String key = tokens[0].trim();
            String value = tokens[1].trim();
            return (criteria != null) ? criteria.and(key).is(value) : Criteria.where(key).is(value);
        } else LOG.warn("Error reading criteria: " + Arrays.asList(tokens)+ ", must contain exactly 2 elements");

        return criteria;
    }

    private Criteria createLikeCriteria(Criteria criteria, String[] tokens) {
        if (tokens.length == 2) {
            String key = tokens[0].trim();
            String value = tokens[1].trim();
            return (criteria != null) ? criteria.and(key).regex(value) : Criteria.where(key).regex(value);
        } else LOG.warn("Error reading criteria: " + Arrays.asList(tokens)+ ", must contain exactly 2 elements");

        return criteria;
    }

    private Criteria createGtCriteria(Criteria criteria, String[] tokens) {
        if (tokens.length == 2) {
            String key = tokens[0].trim();
            String value = tokens[1].trim();
            return (criteria != null) ? criteria.and(key).gt(value) : Criteria.where(key).gt(value);
        } else LOG.warn("Error reading criteria: " + Arrays.asList(tokens)+ ", must contain exactly 2 elements");

        return criteria;
    }

    private Criteria createLtCriteria(Criteria criteria, String[] tokens) {
        if (tokens.length == 2) {
            String key = tokens[0].trim();
            String value = tokens[1].trim();
            return (criteria != null) ? criteria.and(key).lt(value) : Criteria.where(key).lt(value);
        } else LOG.warn("Error reading criteria: " + Arrays.asList(tokens)+ ", must contain exactly 2 elements");

        return criteria;
    }

}
