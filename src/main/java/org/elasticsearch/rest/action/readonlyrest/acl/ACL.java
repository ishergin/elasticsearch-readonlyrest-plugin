package org.elasticsearch.rest.action.readonlyrest.acl;

import java.util.List;
import java.util.Map;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.action.readonlyrest.acl.Rule.Type;

public class ACL {

  private Settings            s;
  private ESLogger            logger;
  private List<Rule>          rules  = Lists.newArrayList();
  private final static String PREFIX = "readonlyrest.access_control_rules";

  public ACL(ESLogger logger, Settings s) throws Exception {
    this.logger = logger;
    this.s = s;
    readRules();
  }

  private void readRules() throws Exception {
    Map<String, Settings> g = s.getGroups(PREFIX);
    for (String k : g.keySet()) {
      Rule r = Rule.build(g.get(k));
      rules.add(r);
      logger.info(r.toString());
    }

  }

  /**
   * Check the request against configured ACL rules. This does not work with try/catch because stacktraces are expensive
   * for performance.
   * 
   * @param r
   * @return null if request pass the rules or the name of the first violated rule
   */
  public String check(ACLRequest req) {
    for (Rule rule : rules) {
      // The logic will exit at the first rule that matches the request
      boolean match = true;
      match &= rule.matchesAddress(req.getAddress());
      match &= rule.matchesMaxBodyLength(req.getMaxBodyLenght());
      match &= rule.matchesUriRe(req.getUri());
      match &= rule.mathesMethods(req.getMethod());
      
      if(match){
        return rule.type.equals(Type.FORBID) ? rule.name : null;
      }
    }
    return "request matches no rules, forbidden by default";
  }

}
