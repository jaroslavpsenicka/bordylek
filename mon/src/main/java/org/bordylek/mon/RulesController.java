package org.bordylek.mon;

import org.drools.KnowledgeBase;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/rest")
public class RulesController {

	@Autowired
	private KnowledgeBase knowledgeBase;

	@Autowired
	private ConfigurationService config;

	private static final Logger LOG = LoggerFactory.getLogger(RulesController.class);

	@RequestMapping(value = "/rules", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Map<String, Map<String, List<RuleDef>>> getRules() {
		final Set<String> disabledRules = config.getDisabledRules();
		final Map<String, List<RuleDef>> rules = new HashMap<>();
		for (KnowledgePackage knowledgePackage : knowledgeBase.getKnowledgePackages()) {
			List<RuleDef> defs = new ArrayList<>();
			rules.put(knowledgePackage.getName(), defs);
			for (Rule rule : knowledgePackage.getRules()) {
				String fqRuleName = knowledgePackage.getName() + "." + rule.getName();
				defs.add(new RuleDef(knowledgePackage.getName(), rule.getName(), !disabledRules.contains(fqRuleName)));
			}
		}

		return new HashMap<String, Map<String, List<RuleDef>>>() {{
			put("data", rules);
		}};
	}

	@RequestMapping(value = "/rules/toggle", method = RequestMethod.POST,
		consumes = "application/json", produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public RuleDef toggleRule(@RequestBody String fqRuleName) {
		RuleDef ruleDef = new RuleDef(fqRuleName);
		Set<String> disabledRules = config.getDisabledRules();
		if (disabledRules.contains(fqRuleName)) {
			config.enableRule(fqRuleName);
			ruleDef.setEnabled(true);
		} else {
			config.disableRule(fqRuleName);
			ruleDef.setEnabled(false);
		}

		return ruleDef;
	}

	public static class RuleDef {

		private String packageName;
		private String name;
		private boolean enabled;

		public RuleDef() {
		}

		public RuleDef(String fqName) {
			int lastIdx = fqName.lastIndexOf('.');
			this.packageName = fqName.substring(0, lastIdx);
			this.name = fqName.substring(0, lastIdx + 1);
		}

		public RuleDef(String packageName, String name) {
			this.packageName = packageName;
			this.name = name;
		}

		public RuleDef(String packageName, String name, boolean enabled) {
			this(packageName, name);
			this.enabled = enabled;
		}

		public String getPackageName() {
			return packageName;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
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
