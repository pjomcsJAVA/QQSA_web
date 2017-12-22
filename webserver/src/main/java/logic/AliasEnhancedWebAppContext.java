package logic;

import org.eclipse.jetty.webapp.WebAppContext;

import java.util.Map;

public class AliasEnhancedWebAppContext extends WebAppContext {
    @Override
    public String getResourceAlias(String alias) {

        @SuppressWarnings("unchecked")
        Map<String, String> resourceAliases =
                (Map<String, String>) getResourceAliases();

        if (resourceAliases == null) {
            return null;
        }

        for (Map.Entry<String, String> oneAlias :
                resourceAliases.entrySet()) {

            if (alias.startsWith(oneAlias.getKey())) {
                return alias.replace(
                        oneAlias.getKey(), oneAlias.getValue());
            }
        }
        return null;
    }
}
