package org.dllearner.confparser;

import org.dllearner.cli.ConfFileOption;
import org.dllearner.core.AbstractComponent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Config parser base class
 */
public abstract class AbstractConfParser extends AbstractComponent implements ConfParser {

        // special directives (those without a property name)
        protected Map<String, ConfFileOption> specialOptions = new HashMap<String, ConfFileOption>();

        // conf file options
        protected List<ConfFileOption> confOptions = new LinkedList<ConfFileOption>();
        protected Map<String, ConfFileOption> confOptionsByProperty = new HashMap<String, ConfFileOption>();
        protected Map<String, List<ConfFileOption>> confOptionsByBean = new HashMap<String, List<ConfFileOption>>();


        public List<ConfFileOption> getConfOptions() {
                return confOptions;
        }

        public Map<String, ConfFileOption> getConfOptionsByProperty() {
                return confOptionsByProperty;
        }

        public ConfFileOption getConfOptionsByProperty(String propertyName) {
                ConfFileOption confOption = confOptionsByProperty.get(propertyName);
                if (confOption == null) {
                        confOption = specialOptions.get(propertyName);
                }
                return confOption;
        }

        public Map<String, List<ConfFileOption>> getConfOptionsByBean() {
                return confOptionsByBean;
        }

        public List<ConfFileOption> getConfOptionsByBean(String beanName) {
                return confOptionsByBean.get(beanName);
        }

        /**
         * add the confg option after finished parsing
         * @param confOption
         */
        protected void addConfOption(ConfFileOption confOption) {
                if (confOption.getPropertyName() == null) {
                        specialOptions.put(confOption.getBeanName(), confOption);
                } else {
                        confOptions.add(confOption);
                        confOptionsByProperty.put(confOption.getPropertyName(), confOption);
                        String beanName = confOption.getBeanName();
                        if (confOptionsByBean.containsKey(beanName))
                                confOptionsByBean.get(beanName).add(confOption);
                        else {
                                LinkedList<ConfFileOption> optionList = new LinkedList<ConfFileOption>();
                                optionList.add(confOption);
                                confOptionsByBean.put(beanName, optionList);
                        }
                }
        }

        /**
         * important, this method must be called from init()
         */
        protected void postProcess() {
                PostProcessor pp = new PostProcessor(confOptions, specialOptions);
                pp.applyAll();
        }

}
