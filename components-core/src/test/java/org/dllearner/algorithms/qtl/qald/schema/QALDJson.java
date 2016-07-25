
package org.dllearner.algorithms.qtl.qald.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "dataset",
    "questions"
})
public class QALDJson {

    @JsonProperty("dataset")
    private Dataset dataset;
    @JsonProperty("questions")
    private List<Question> questions = new ArrayList<Question>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The dataset
     */
    @JsonProperty("dataset")
    public Dataset getDataset() {
        return dataset;
    }

    /**
     * 
     * @param dataset
     *     The dataset
     */
    @JsonProperty("dataset")
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * 
     * @return
     *     The questions
     */
    @JsonProperty("questions")
    public List<Question> getQuestions() {
        return questions;
    }

    /**
     * 
     * @param questions
     *     The questions
     */
    @JsonProperty("questions")
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
