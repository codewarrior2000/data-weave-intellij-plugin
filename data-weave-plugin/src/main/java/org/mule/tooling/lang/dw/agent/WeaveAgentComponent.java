package org.mule.tooling.lang.dw.agent;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.net.NetUtils;
import org.jetbrains.annotations.NotNull;
import org.mule.tooling.lang.dw.launcher.configuration.runner.WeaveRunnerHelper;
import org.mule.weave.v2.debugger.client.DefaultWeaveAgentClientListener;
import org.mule.weave.v2.debugger.client.WeaveAgentClient;
import org.mule.weave.v2.debugger.client.tcp.TcpClientProtocol;
import org.mule.weave.v2.debugger.event.*;

import java.io.IOException;

public class WeaveAgentComponent extends AbstractProjectComponent {


    public static final int MAX_RETRIES = 100;
    public static final long ONE_SECOND_DELAY = 1000L;

    private WeaveAgentClient client;
    private ProcessHandler processHandler;

    protected WeaveAgentComponent(Project project) {
        super(project);
    }

    public void init() {
        int freePort;
        if (processHandler != null) {
            tearDown();
        }
        try {
            freePort = NetUtils.findAvailableSocketPort();
        } catch (IOException e) {
            freePort = 2333;
        }
        final ProgramRunner runner = new DefaultProgramRunner() {
            @Override
            @NotNull
            public String getRunnerId() {
                return "Weave Agent Runner";
            }

            @Override
            public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
                return true;
            }
        };
        final Executor executor = DefaultRunExecutor.getRunExecutorInstance();

        try {
            RunProfileState state = createRunProfileState(freePort);
            ExecutionResult result = state.execute(executor, runner);
            //noinspection ConstantConditions
            processHandler = result.getProcessHandler();
            //Wait for it to init
            processHandler.waitFor(ONE_SECOND_DELAY);
        } catch (Throwable e) {
            return;
        }
        processHandler.startNotify();
        TcpClientProtocol clientProtocol = new TcpClientProtocol("localhost", freePort);
        client = new WeaveAgentClient(clientProtocol, new DefaultWeaveAgentClientListener());
        client.connect(MAX_RETRIES, 1000L);
    }

    public void runPreview(String inputsPath, String script, String identifier, String url, Long maxTime, RunPreviewCallback callback) {
        checkClientConnected(() -> {
                    //Make sure all files are persisted before running preview
                    ApplicationManager.getApplication().invokeLater(() -> {
                        FileDocumentManager.getInstance().saveAllDocuments();
                        client.runPreview(inputsPath, script, identifier, url, maxTime, new DefaultWeaveAgentClientListener() {
                            @Override
                            public void onPreviewExecuted(PreviewExecutedEvent result) {
                                ApplicationManager.getApplication().invokeLater(() -> {
                                    if (result instanceof PreviewExecutedSuccessfulEvent) {
                                        PreviewExecutedSuccessfulEvent successfulEvent = (PreviewExecutedSuccessfulEvent) result;
                                        callback.onPreviewSuccessful(successfulEvent);
                                    } else if (result instanceof PreviewExecutedFailedEvent) {
                                        callback.onPreviewFailed((PreviewExecutedFailedEvent) result);
                                    }

                                });

                            }
                        });
                    });
                }
        );
    }

    public void checkClientConnected(Runnable onConnected) {
        if (client == null || !client.isConnected()) {
            init();
        }
        if (client.isConnected()) {
            onConnected.run();
        } else {
            Notifications.Bus.notify(new Notification("Data Weave", "Unable to connect", "Client is not able to connect to runtime", NotificationType.ERROR));
        }
    }

    public boolean isWeaveRuntimeInstalled() {
        GlobalSearchScope scope = GlobalSearchScope.allScope(myProject);
        PsiClass c = JavaPsiFacade.getInstance(myProject).findClass(getMarkerClassFQName(), scope);
        return c != null;
    }

    private String getMarkerClassFQName() {
        //The class from the agent runtime
        return "org.mule.weave.v2.runtime.utils.AgentCustomRunner";
    }


    public void calculateImplicitInputTypes(String inputsPath, ImplicitInputTypesCallback callback) {
        checkClientConnected(() -> {
            //Make sure all files are persisted before running preview
            ApplicationManager.getApplication().invokeLater(() -> {
                FileDocumentManager.getInstance().saveAllDocuments();
                client.inferWeaveType(inputsPath, new DefaultWeaveAgentClientListener() {
                    @Override
                    public void onImplicitWeaveTypesCalculated(ImplicitInputTypesEvent result) {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            callback.onInputsTypesCalculated(result);
                        });
                    }
                });
            });
        });
    }

    public void dataFormats(DataFormatsDefinitionCallback callback) {
        checkClientConnected(() -> {
            client.definedDataFormats(new DefaultWeaveAgentClientListener() {
                @Override
                public void onDataFormatDefinitionCalculated(DataFormatsDefinitionsEvent dfde) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        callback.onDataFormatsLoaded(dfde);
                    });
                }
            });
        });
    }

    public void resolveModule(String identifier, String loader, ModuleLoadedCallback callback) {
        checkClientConnected(() -> {
            client.resolveModule(identifier, loader, new DefaultWeaveAgentClientListener() {
                @Override
                public void onModuleLoaded(ModuleResolvedEvent result) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        callback.onModuleResolved(result);
                    });
                }
            });
        });
    }

    public void tearDown() {
        //Kill the process
        processHandler.destroyProcess();
        client.disconnect();
    }


    private RunProfileState createRunProfileState(int freePort) {
        return new CommandLineState(null) {
            private SimpleJavaParameters createJavaParameters() {
                final SimpleJavaParameters params = WeaveRunnerHelper.createJavaParameters(myProject);
                params.getVMParametersList().add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5678");
                ParametersList parametersList = params.getProgramParametersList();
                parametersList.add("--agent");
                parametersList.add("-p");
                parametersList.add(String.valueOf(freePort));
                return params;
            }

            @NotNull
            @Override
            public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
                ProcessHandler processHandler = startProcess();
                return new DefaultExecutionResult(null, processHandler);
            }

            @Override
            @NotNull
            protected OSProcessHandler startProcess() throws ExecutionException {
                SimpleJavaParameters params = createJavaParameters();
                GeneralCommandLine commandLine = params.toCommandLine();
                OSProcessHandler processHandler = new OSProcessHandler(commandLine);
                processHandler.setShouldDestroyProcessRecursively(false);
                return processHandler;
            }
        };
    }

    public static WeaveAgentComponent getInstance(@NotNull Project project) {
        return project.getComponent(WeaveAgentComponent.class);
    }

}