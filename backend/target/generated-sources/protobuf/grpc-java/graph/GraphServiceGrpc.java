package graph;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * 定义图服务
 * </pre>
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 1.46.0)",
        comments = "Source: graph.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class GraphServiceGrpc {

    private GraphServiceGrpc() {
    }

    public static final String SERVICE_NAME = "graph.GraphService";

    // Static method descriptors that strictly reflect the proto.
    private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
            graph.Graph.TripleList> getGetTriplesMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "GetTriples",
            requestType = com.google.protobuf.Empty.class,
            responseType = graph.Graph.TripleList.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
            graph.Graph.TripleList> getGetTriplesMethod() {
        io.grpc.MethodDescriptor<com.google.protobuf.Empty, graph.Graph.TripleList> getGetTriplesMethod;
        if ((getGetTriplesMethod = GraphServiceGrpc.getGetTriplesMethod) == null) {
            synchronized (GraphServiceGrpc.class) {
                if ((getGetTriplesMethod = GraphServiceGrpc.getGetTriplesMethod) == null) {
                    GraphServiceGrpc.getGetTriplesMethod = getGetTriplesMethod =
                            io.grpc.MethodDescriptor.<com.google.protobuf.Empty, graph.Graph.TripleList>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTriples"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.google.protobuf.Empty.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            graph.Graph.TripleList.getDefaultInstance()))
                                    .setSchemaDescriptor(new GraphServiceMethodDescriptorSupplier("GetTriples"))
                                    .build();
                }
            }
        }
        return getGetTriplesMethod;
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static GraphServiceStub newStub(io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<GraphServiceStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<GraphServiceStub>() {
                    @java.lang.Override
                    public GraphServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new GraphServiceStub(channel, callOptions);
                    }
                };
        return GraphServiceStub.newStub(factory, channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static GraphServiceBlockingStub newBlockingStub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<GraphServiceBlockingStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<GraphServiceBlockingStub>() {
                    @java.lang.Override
                    public GraphServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new GraphServiceBlockingStub(channel, callOptions);
                    }
                };
        return GraphServiceBlockingStub.newStub(factory, channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static GraphServiceFutureStub newFutureStub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<GraphServiceFutureStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<GraphServiceFutureStub>() {
                    @java.lang.Override
                    public GraphServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new GraphServiceFutureStub(channel, callOptions);
                    }
                };
        return GraphServiceFutureStub.newStub(factory, channel);
    }

    /**
     * <pre>
     * 定义图服务
     * </pre>
     */
    public static abstract class GraphServiceImplBase implements io.grpc.BindableService {

        /**
         * <pre>
         * 获取三元组列表
         * </pre>
         */
        public void getTriples(com.google.protobuf.Empty request,
                               io.grpc.stub.StreamObserver<graph.Graph.TripleList> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTriplesMethod(), responseObserver);
        }

        @java.lang.Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            getGetTriplesMethod(),
                            io.grpc.stub.ServerCalls.asyncUnaryCall(
                                    new MethodHandlers<
                                            com.google.protobuf.Empty,
                                            graph.Graph.TripleList>(
                                            this, METHODID_GET_TRIPLES)))
                    .build();
        }
    }

    /**
     * <pre>
     * 定义图服务
     * </pre>
     */
    public static final class GraphServiceStub extends io.grpc.stub.AbstractAsyncStub<GraphServiceStub> {
        private GraphServiceStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected GraphServiceStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new GraphServiceStub(channel, callOptions);
        }

        /**
         * <pre>
         * 获取三元组列表
         * </pre>
         */
        public void getTriples(com.google.protobuf.Empty request,
                               io.grpc.stub.StreamObserver<graph.Graph.TripleList> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getGetTriplesMethod(), getCallOptions()), request, responseObserver);
        }
    }

    /**
     * <pre>
     * 定义图服务
     * </pre>
     */
    public static final class GraphServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<GraphServiceBlockingStub> {
        private GraphServiceBlockingStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected GraphServiceBlockingStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new GraphServiceBlockingStub(channel, callOptions);
        }

        /**
         * <pre>
         * 获取三元组列表
         * </pre>
         */
        public graph.Graph.TripleList getTriples(com.google.protobuf.Empty request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getGetTriplesMethod(), getCallOptions(), request);
        }
    }

    /**
     * <pre>
     * 定义图服务
     * </pre>
     */
    public static final class GraphServiceFutureStub extends io.grpc.stub.AbstractFutureStub<GraphServiceFutureStub> {
        private GraphServiceFutureStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected GraphServiceFutureStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new GraphServiceFutureStub(channel, callOptions);
        }

        /**
         * <pre>
         * 获取三元组列表
         * </pre>
         */
        public com.google.common.util.concurrent.ListenableFuture<graph.Graph.TripleList> getTriples(
                com.google.protobuf.Empty request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getGetTriplesMethod(), getCallOptions()), request);
        }
    }

    private static final int METHODID_GET_TRIPLES = 0;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final GraphServiceImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(GraphServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_GET_TRIPLES:
                    serviceImpl.getTriples((com.google.protobuf.Empty) request,
                            (io.grpc.stub.StreamObserver<graph.Graph.TripleList>) responseObserver);
                    break;
                default:
                    throw new AssertionError();
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(
                io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                default:
                    throw new AssertionError();
            }
        }
    }

    private static abstract class GraphServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
        GraphServiceBaseDescriptorSupplier() {
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return graph.Graph.getDescriptor();
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("GraphService");
        }
    }

    private static final class GraphServiceFileDescriptorSupplier
            extends GraphServiceBaseDescriptorSupplier {
        GraphServiceFileDescriptorSupplier() {
        }
    }

    private static final class GraphServiceMethodDescriptorSupplier
            extends GraphServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
        private final String methodName;

        GraphServiceMethodDescriptorSupplier(String methodName) {
            this.methodName = methodName;
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
            return getServiceDescriptor().findMethodByName(methodName);
        }
    }

    private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

    public static io.grpc.ServiceDescriptor getServiceDescriptor() {
        io.grpc.ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (GraphServiceGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new GraphServiceFileDescriptorSupplier())
                            .addMethod(getGetTriplesMethod())
                            .build();
                }
            }
        }
        return result;
    }
}
