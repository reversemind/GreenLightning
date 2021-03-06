package com.ociweb.gl.json;

import com.ociweb.gl.api.*;
import com.ociweb.json.appendable.StringBuilderWriter;
import com.ociweb.pronghorn.pipe.ChannelReader;

public class JSONClientBehavior implements HTTPResponseListener, StartupListener {
    private final GreenRuntime runtime;
    private final GreenCommandChannel command;
    private final ClientHostPortInstance session;

    JSONClientBehavior(GreenRuntime runtime, ClientHostPortInstance session) {
        this.runtime = runtime;
        this.command = runtime.newCommandChannel();
        this.command.ensureHTTPClientRequesting();
        this.command.ensureDynamicMessaging();
        this.session = session;
    }

    @Override
    public void startup() {
        JSONRequest request1 = new JSONRequest();
        request1.setId1("Chesterfield");
        request1.setId2("12345657890");
        request1.setValue(42);

        JSONRequest request2 = new JSONRequest();
        request2.setId1("Bridgeton");
        request2.setId2("0987654321");
        request2.setValue(43);

        StringBuilderWriter out = new StringBuilderWriter();
        JSONRequest.renderer.render(out, request1);
        System.out.println("Client JSON 1:" + out);

        out.setLength(0);
        JSONRequest.renderer.render(out, request2);
        System.out.println("Client JSON 2:" + out);

        command.httpPost(session, "/test/path?flag=42", writer -> JSONRequest.renderer.render(writer, request1));
        command.httpPost(session, "/test/path", writer -> JSONRequest.renderer.render(writer, request2));
    }

    @Override
    public boolean responseHTTP(HTTPResponseReader httpResponseReader) {
        System.out.println("Client received response.");
        httpResponseReader.openPayloadData(new Payloadable() {
            @Override
            public void read(ChannelReader reader) {
                System.out.println(reader.readUTFFully());
            }
        });
        runtime.shutdownRuntime();
        return true;
    }
}
