package com.issinc.counsel.dllearner;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.OntologyFormatUnsupportedException;
import org.dllearner.core.configurators.Configurator;
import org.dllearner.core.owl.KB;
import org.springframework.core.io.ByteArrayResource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Jul 9, 2010
 * Time: 2:20:09 PM
 * <p/>
 * This class provides a ITemporalSnapshot based implementation of the DL Learner Knoweldge Source
 */
public class CombinedModelKnowledgeSource extends KnowledgeSource {


    private ICombinedModel combinedModel;


    /**
     * Default Constructor
     */
    public CombinedModelKnowledgeSource() {

    }

    /**
     * Get this Knowledge Source as An InputStream.  This way we can load it without having a physical file.
     *
     * @return
     */
    public InputStream toInputStream() {

        InputStream result;

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            combinedModel.getModel().write(bos,"RDF/XML-ABBREV");
            bos.close();

            ByteArrayResource bar = new ByteArrayResource(bos.toByteArray());

            result = bar.getInputStream();
        }

        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public KB toKB() {
        throw new UnsupportedOperationException("This operation is not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toDIG(URI kbURI) {
        throw new UnsupportedOperationException("This operation is not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void export(File file, OntologyFormat format) throws OntologyFormatUnsupportedException {
        // currently no export functions implemented, so we just throw an exception
        throw new OntologyFormatUnsupportedException("export", format);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configurator getConfigurator() {
        throw new UnsupportedOperationException("This operation is not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() throws ComponentInitException {
        throw new UnsupportedOperationException("This operation is not yet implemented");
    }

    /**
     * Get the associated combined model.
     *
     * @return The associated combined model.
     */
    public ICombinedModel getCombinedModel() {
        return combinedModel;
    }

    /**
     * Set the associated combined model.
     *
     * @param combinedModel The associated combined model.
     */
    public void setCombinedModel(ICombinedModel combinedModel) {
        this.combinedModel = combinedModel;
    }
}
