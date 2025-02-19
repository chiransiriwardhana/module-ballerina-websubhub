/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.websubhub;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.ArrayList;

import static io.ballerina.runtime.api.utils.StringUtils.fromString;

/**
 * {@code HubNativeOprationHandler} handles the native method invocations.
 * For every remote method there is a seperate method to be called
 * with {@code Environment}, {@code BObject} hubService and {@code BMap} message.
 */
public class HubNativeOperationHandler {

    public static BArray getServiceMethodNames(BObject bHubService) {
        ArrayList<BString> methodNamesList = new ArrayList<>();
        for (MethodType method : bHubService.getType().getMethods()) {
            methodNamesList.add(StringUtils.fromString(method.getName()));
        }
        return ValueCreator.createArrayValue(methodNamesList.toArray(BString[]::new));
    }

    public static Object callRegisterMethod(Environment env, BObject bHubService, 
                                            BMap<BString, Object> message, BObject bHttpHeaders) {
        Object[] args = new Object[]{message, true, bHttpHeaders, true};
        return invokeRemoteFunction(env, bHubService, args, "callRegisterMethod", "onRegisterTopic");
    }

    public static Object callDeregisterMethod(Environment env, BObject bHubService, 
                                              BMap<BString, Object> message, BObject bHttpHeaders) {
        Object[] args = new Object[]{message, true, bHttpHeaders, true};
        return invokeRemoteFunction(env, bHubService, args, "callDeregisterMethod", "onDeregisterTopic");
    }

    public static Object callOnUpdateMethod(Environment env, BObject bHubService, 
                                            BMap<BString, Object> message, BObject bHttpHeaders) {
        Object[] args = new Object[]{message, true, bHttpHeaders, true};
        return invokeRemoteFunction(env, bHubService, args, "callOnUpdateMethod", "onUpdateMessage");
    }

    public static Object callOnSubscriptionMethod(Environment env, BObject bHubService, 
                                                  BMap<BString, Object> message, BObject bHttpHeaders) {
        Object[] args = new Object[]{message, true, bHttpHeaders, true};
        return invokeRemoteFunction(env, bHubService, args, "callOnSubscriptionMethod", "onSubscription");
    }

    public static Object callOnSubscriptionValidationMethod(Environment env, BObject bHubService, 
                                                            BMap<BString, Object> message, BObject bHttpHeaders) {
        Object[] args = new Object[]{message, true, bHttpHeaders, true};
        return invokeRemoteFunction(env, bHubService, args, "callOnSubscriptionValidationMethod", 
                                    "onSubscriptionValidation");
    }

    public static void callOnSubscriptionIntentVerifiedMethod(Environment env, BObject bHubService, 
                                                              BMap<BString, Object> message, BObject bHttpHeaders) {
        Object[] args = new Object[]{message, true, bHttpHeaders, true};
        invokeRemoteFunction(env, bHubService, args, "callOnSubscriptionIntentVerifiedMethod", 
                            "onSubscriptionIntentVerified");
    }

    public static Object callOnUnsubscriptionMethod(Environment env, BObject bHubService, 
                                                    BMap<BString, Object> message, BObject bHttpHeaders) {
        Object[] args = new Object[]{message, true, bHttpHeaders, true};
        return invokeRemoteFunction(env, bHubService, args, "callOnUnsubscriptionMethod", "onUnsubscription");
    }

    public static Object callOnUnsubscriptionValidationMethod(Environment env, BObject bHubService, 
                                                              BMap<BString, Object> message, BObject bHttpHeaders) {
        Object[] args = new Object[]{message, true, bHttpHeaders, true};
        return invokeRemoteFunction(env, bHubService, args, "callOnUnsubscriptionValidationMethod", 
                                    "onUnsubscriptionValidation");
    }

    public static void callOnUnsubscriptionIntentVerifiedMethod(Environment env, BObject bHubService, 
                                                                BMap<BString, Object> message, BObject bHttpHeaders) {
        Object[] args = new Object[]{message, true, bHttpHeaders, true};
        invokeRemoteFunction(env, bHubService, args, "callOnUnsubscriptionIntentVerifiedMethod", 
                            "onUnsubscriptionIntentVerified");
    }

    private static Object invokeRemoteFunction(Environment env, BObject bHubService, Object[] args,
                                String parentFunctionName, String remoteFunctionName) {
        Future balFuture = env.markAsync();
        Module module = ModuleUtils.getModule();
        StrandMetadata metadata = new StrandMetadata(module.getOrg(), module.getName(), module.getVersion(),
                parentFunctionName);
        env.getRuntime().invokeMethodAsync(bHubService, remoteFunctionName, null, metadata, new Callback() {
            @Override
            public void notifySuccess(Object result) {
                balFuture.complete(result);
            }

            @Override
            public void notifyFailure(BError bError) {
                BString errorMessage = fromString("service method invocation failed: " + bError.getErrorMessage());
                BError invocationError = ErrorCreator.createError(module, "ServiceExecutionError", 
                                                    errorMessage, bError, null);
                balFuture.complete(invocationError);
            }
        }, args);
        return null;
    }

}
