package org.dllearner.algorithms.distributed.amqp;

import javax.jms.CompletionListener;
import javax.jms.Message;

public class DummyCompletionListener implements CompletionListener {

    @Override
    public void onCompletion(Message message) {
        // do nothing...
    }

    @Override
    public void onException(Message message, Exception exception) {
        // do nothing...
    }

}
