/*
 * Copyright 2014 hbz NRW (http://www.hbz-nrw.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package actions;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import models.Node;
import play.mvc.Results.Chunks;
import play.mvc.Results.StringChunks;

/**
 * @author Jan Schnasse
 *
 */
public class BulkAction {

    Chunks.Out<String> messageOut;
    Chunks<String> chunks;
    Read read = new Read();

    /**
     * @param namespace
     *            a namespace to retrieve all pids from
     * @param proc
     *            a function to apply to all pids
     */
    public void execute(String namespace, ProcessNodes proc) {
	chunks = new StringChunks() {
	    public void onReady(Chunks.Out<String> out) {
		setMessageQueue(out);
		ExecutorService executorService = Executors
			.newSingleThreadExecutor();
		executorService.execute(new Runnable() {
		    public void run() {
			bulk(namespace, proc);
		    }
		});
		executorService.shutdown();
	    }
	};
    }

    /**
     * @param nodes
     *            a list of node pids
     * @param proc
     *            a function to apply to all nodes
     */
    public void executeOnPids(List<String> nodes, ProcessNodes proc) {
	chunks = new StringChunks() {
	    public void onReady(Chunks.Out<String> out) {
		setMessageQueue(out);
		ExecutorService executorService = Executors
			.newSingleThreadExecutor();
		executorService.execute(new Runnable() {
		    public void run() {
			bulkOnPids(nodes, proc);
		    }
		});
		executorService.shutdown();
	    }
	};
    }

    /**
     * @param nodes
     *            a list of nodes
     * @param proc
     *            a function to apply to all nodes
     */
    public void executeOnNodes(List<Node> nodes, ProcessNodes proc) {
	chunks = new StringChunks() {
	    public void onReady(Chunks.Out<String> out) {
		setMessageQueue(out);
		ExecutorService executorService = Executors
			.newSingleThreadExecutor();
		executorService.execute(new Runnable() {
		    public void run() {
			bulkOnNodes(nodes, proc);
		    }
		});
		executorService.shutdown();
	    }
	};
    }

    private void bulk(String namespace, ProcessNodes proc) {
	List<String> nodes = read.listRepoNamespace(namespace);
	bulkOnPids(nodes, proc);
    }

    private void bulkOnPids(List<String> nodes, ProcessNodes proc) {
	try {
	    int until = 0;
	    int stepSize = 100;
	    int from = 0 - stepSize;
	    messageOut.write("size: " + nodes.size() + "\n");
	    do {
		until += stepSize;
		from += stepSize;
		if (nodes.isEmpty())
		    break;
		if (until > nodes.size())
		    until = nodes.size();

		messageOut.write("Process: from: " + from + " until " + until
			+ "\n");
		try {
		    messageOut.write(proc.process(read.getNodes(nodes.subList(
			    from, until))));
		} catch (Exception e) {
		    play.Logger.warn("", e);
		}
	    } while (until < nodes.size());
	    messageOut.write("Process " + nodes.size() + " nodes!");
	    messageOut.write("\nSuccessfully Finished\n");
	} catch (Exception e) {
	    play.Logger.error("", e);
	} finally {
	    messageOut.close();
	}
    }

    private void bulkOnNodes(final List<Node> nodes, ProcessNodes proc) {
	try {
	    int until = 0;
	    int stepSize = 100;
	    int from = 0 - stepSize;
	    messageOut.write("size: " + nodes.size() + "\n");
	    do {
		until += stepSize;
		from += stepSize;
		if (nodes.isEmpty())
		    break;
		if (until > nodes.size())
		    until = nodes.size();
		messageOut.write("Process: from: " + from + " until " + until
			+ "\n");
		messageOut.write(proc.process(nodes.subList(from, until)));
	    } while (until < nodes.size());
	    messageOut.write("Process " + nodes.size() + " nodes!");
	    messageOut.write("\nSuccessfully Finished\n");
	} catch (Exception e) {
	    play.Logger.error("", e);
	} finally {
	    messageOut.close();
	}
    }

    /**
     * @param out
     *            messages for chunked responses
     */
    public void setMessageQueue(Chunks.Out<String> out) {
	messageOut = out;
    }

    /**
     * Close messageQueue for chunked responses
     * 
     */
    public void closeMessageQueue() {
	if (messageOut != null)
	    messageOut.close();
    }

    /**
     * @return chunks to be returned by the actual controller action
     */
    public Chunks<String> getChunks() {
	return chunks;
    }
}
