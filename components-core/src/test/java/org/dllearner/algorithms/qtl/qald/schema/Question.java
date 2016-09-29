
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
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "answertype",
    "aggregation",
    "onlydbo",
    "hybrid",
    "question",
    "query",
    "answers"
})
public class Question {

    @JsonProperty("id")
    private int id;
    @JsonProperty("answertype")
    private String answertype;
    @JsonProperty("aggregation")
    private boolean aggregation;
    @JsonProperty("onlydbo")
    private boolean onlydbo;
    @JsonProperty("hybrid")
    private boolean hybrid;
    @JsonProperty("question")
    private List<Question_> question = new ArrayList<Question_>();
    @JsonProperty("query")
    private Query query;
    @JsonProperty("answers")
    private List<Answer> answers = new ArrayList<Answer>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public int getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The answertype
     */
    @JsonProperty("answertype")
    public String getAnswertype() {
        return answertype;
    }

    /**
     * 
     * @param answertype
     *     The answertype
     */
    @JsonProperty("answertype")
    public void setAnswertype(String answertype) {
        this.answertype = answertype;
    }

    /**
     * 
     * @return
     *     The aggregation
     */
    @JsonProperty("aggregation")
    public boolean isAggregation() {
        return aggregation;
    }

    /**
     * 
     * @param aggregation
     *     The aggregation
     */
    @JsonProperty("aggregation")
    public void setAggregation(boolean aggregation) {
        this.aggregation = aggregation;
    }

    /**
     * 
     * @return
     *     The onlydbo
     */
    @JsonProperty("onlydbo")
    public boolean isOnlyDBO() {
        return onlydbo;
    }

    /**
     * 
     * @param onlydbo
     *     The onlydbo
     */
    @JsonProperty("onlydbo")
    public void setOnlydbo(boolean onlydbo) {
        this.onlydbo = onlydbo;
    }

    /**
     * 
     * @return
     *     The hybrid
     */
    @JsonProperty("hybrid")
    public boolean isHybrid() {
        return hybrid;
    }

    /**
     * 
     * @param hybrid
     *     The hybrid
     */
    @JsonProperty("hybrid")
    public void setHybrid(boolean hybrid) {
        this.hybrid = hybrid;
    }

    /**
     * 
     * @return
     *     The question
     */
    @JsonProperty("question")
    public List<Question_> getQuestion() {
        return question;
    }

    /**
     * 
     * @param question
     *     The question
     */
    @JsonProperty("question")
    public void setQuestion(List<Question_> question) {
        this.question = question;
    }

    /**
     * 
     * @return
     *     The query
     */
    @JsonProperty("query")
    public Query getQuery() {
        return query;
    }

    /**
     * 
     * @param query
     *     The query
     */
    @JsonProperty("query")
    public void setQuery(Query query) {
        this.query = query;
    }

    /**
     * 
     * @return
     *     The answers
     */
    @JsonProperty("answers")
    public List<Answer> getAnswers() {
        return answers;
    }

    /**
     * 
     * @param answers
     *     The answers
     */
    @JsonProperty("answers")
    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
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
