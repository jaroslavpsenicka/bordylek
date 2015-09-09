package org.bordylek.mon;

import org.drools.KnowledgeBase;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class RulesController {

	@Autowired
	private KnowledgeBase knowledgeBase;

	@Autowired
	private ConfigurationService config;

	private static final Logger LOG = LoggerFactory.getLogger(RulesController.class);

	@RequestMapping(value = "/rules", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Map<String, List<RuleDef>> getRules() {
		Set<String> disabledRules = config.getDisabledRules();
		Map<String, List<RuleDef>> rules = new HashMap<>();
		for (KnowledgePackage knowledgePackage : knowledgeBase.getKnowledgePackages()) {
			List<RuleDef> defs = new ArrayList<>();
			rules.put(knowledgePackage.getName(), defs);
			for (Rule rule : knowledgePackage.getRules()) {
				String fqRuleName = knowledgePackage.getName() + "." + rule.getName();
				defs.add(new RuleDef(fqRuleName, !disabledRules.contains(fqRuleName)));
			}
		}

		return rules;
	}

	@RequestMapping(value = "/rules/toggle", method = RequestMethod.POST, consumes = "application/json")
	@ResponseStatus(HttpStatus.OK)
	public void toggleRule(@RequestBody String fqRuleName) {
		Set<String> disabledRules = config.getDisabledRules();
		if (disabledRules.contains(fqRuleName)) {
			config.enableRule(fqRuleName);
		} else {
			config.disableRule(fqRuleName);
		}
	}

	public static class RuleDef {

		private String name;
		private boolean enabled;

		public RuleDef() {
		}

		public RuleDef(String name, boolean enabled) {
			this.name = name;
			this.enabled = enabled;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}

}
