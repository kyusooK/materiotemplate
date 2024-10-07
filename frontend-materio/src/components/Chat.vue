<template>
    <div>
        <v-btn @click="handleClick"
                class="chat-open-btn"
                color="primary"
                icon 
                dark
                large
        >
            <v-icon v-if="openChatBox">mdi-close</v-icon>
            <img v-else src="chatgpt.svg" class="chatgpt-icon" />
        </v-btn>

        <v-card v-if="openChatBox" class="chat-open-box">
            <v-card-text class="message-box" ref="messages">
                <div v-for="(message, index) in messages"
                        :key="index"
                >
                    <div v-if="message.role == 'user'"
                            class="d-flex justify-end my-2"
                    >
                        <div class="user-message">
                            {{ message.text }} 
                        </div>
                        <div class="ml-1">
                            <v-avatar size="48">
                                <v-icon>
                                    mdi-account-circle
                                </v-icon>
                            </v-avatar>
                            <div class="subtitle-2 text-center">
                                {{ message.role }}
                            </div>
                        </div>
                    </div>

                    <div v-else-if="message.role == 'system'"
                            class="d-flex justify-start my-2"
                    >
                        <div class="mr-2">
                            <v-avatar size="48">
                                <v-icon>
                                    mdi-account-circle
                                </v-icon>
                            </v-avatar>
                            <div class="subtitle-2 text-center">
                                {{ message.role }}
                            </div>
                        </div>
                        <div class="d-flex system-message">
                            <div v-html="message.text"></div>
                            <v-btn v-if="message.command"
                                    @click="doit(message)"
                                    color="success"
                                    class="mx-1"
                            >
                                실행
                            </v-btn>
                        </div>
                    </div>
                </div>

                <div v-if="loading" class="d-flex justify-start my-2">
                    <div class="mr-2">
                        <v-avatar size="48">
                            <v-icon>
                                mdi-account-circle
                            </v-icon>
                        </v-avatar>
                        <div class="subtitle-2 text-center">
                            system
                        </div>
                    </div>
                    <div class="d-flex system-message">
                        <v-progress-circular
                                indeterminate
                                color="grey"
                        ></v-progress-circular>
                    </div>
                </div>
            </v-card-text>

            <v-card-actions class="chat-box">
                <v-text-field
                        v-model="newMessage"
                        @keydown.enter="sendMessage"
                        label="Send Message"
                        dense
                        class="px-2"
                >
                    <template v-slot:append>
                        <v-btn @click="sendMessage"
                                color="primary"
                                icon
                                small
                        >
                            <v-icon>mdi-send</v-icon>
                        </v-btn>
                    </template>
                </v-text-field>
            </v-card-actions>
        </v-card>
    </div>
</template>

<script>
import ChatGenerator from "./ai/ChatGenerator.js";
import apiSpec from "./api/openapi.js";

import BaseRepository from "./repository/BaseRepository";
import axios from "@axios";

export default {
    name: 'Chat',
    data: () => ({
        messages: [],
        newMessage: "",
        generator: null,
        loading: false,
        openChatBox: false,
    }),
    created() {
        this.generator = new ChatGenerator(this, {
            isStream: true,
            preferredLanguage: "Korean"
        });
        this.init();
    },
    watch: {
        messages() {
            this.$nextTick(() => {
                let messages = this.$refs.messages;
                messages.scrollTo({ top: messages.scrollHeight, behavior: 'smooth' });
            });
        },
    },
    methods:{
        handleClick() {
            this.openChatBox = !this.openChatBox;
        },
        init() {
            this.generator.previousMessages = [{
            role: 'system',
            content: `
시스템과 사용자간 챗봇을 제공하려고 해.

CONSTRAINTS:

1. ~4000 word limit for short term memory. Your short term memory is short, so immediately save important information to files.
2. If you are unsure how you previously did something or want to recall past events, thinking about similar events will help you remember.
3. If you are unsure about any of arguments from user input, you have to create an error for user to know which argument should be input again.
4. Exclusively use the commands listed in double quotes e.g. "command name"

COMMANDS:

commands are described with OpenAPI3 as below:
${apiSpec}

ERRORS:

1. Incomplete Arguments: "INCOMPLETE-ARGS"

PERFORMANCE EVALUATION:

1. Continuously review and analyze your actions to ensure you are performing to the best of your abilities.
2. Constructively self-criticize your big-picture behavior constantly.
3. Reflect on past decisions and strategies to refine your approach.
4. Every command has a cost, so be smart and efficient. Aim to complete tasks in the least number of steps.

You should only respond in JSON format as described below (don't skip the thoughts and speak at least)

RESPONSE FORMAT:
[{
    "command": {
        "name": "command name",
        "args":{
            "key": "value"
        },
        "path": "api path",
        "method": "http Method" // in capital letters
    },
    "thoughts": {
        "text": "thought",
        "reasoning": "reasoning",
        "plan": [short bulleted,list that conveys,long-term plan],
        "criticism": "constructive self-criticism",
        "speak": "thoughts summary to say to user"
    },
    "error": {
        "name": "error name",
        "speak": "error message to user"
    }
}]
`
            }];
        },

        sendMessage() {
            if (this.newMessage !== "") {
                this.loading = true;
                this.init();
                
                this.messages.push(
                    {
                        role: "user",
                        text: this.newMessage
                    }
                );

                this.generator.generate();
                this.newMessage = "";
            }
        },

        onGenerationFinished(responses){
            console.log(responses);
            this.loading = false;
            var message;

            responses.forEach(response=> {
                if (response.command) {
                    message = {
                        role:'system',
                        text: response.thoughts && response.thoughts.speak ? response.thoughts.speak 
                            : response.command.name + "을 다음의 아규먼트로 실행합니다: ", 
                                // + JSON.stringify(response.command.args),
                        command: response.command
                    };

                    this.messages.push(message);

                } else if (response.markdown) {
                    message = {
                        role:'system',
                        text: response.markdown.replace(/(?:\r\n|\r|\n)/g, '<br />')
                    };

                    this.messages.push(message);

                }  else if (response.error) {
                    message = {
                        role:'system',
                        text: response.error.speak ? response.error.speak : response.error
                    };

                    this.messages.push(message);

                }
            });
        },

        onError(error) {
            this.loading = false;

            if (error.code === "invalid_api_key") {
                var apiKey = prompt("API Key 를 입력하세요.");
                localStorage.setItem("openAIToken", apiKey);
                this.generator.generate();
                
            } else {
                var message = {
                    role:'system',
                    text: error.message
                };

                this.messages.push(message);
            }
        },

        async doit(message){
            this.loading = true;

            if (message.command.method === 'GET') {
                var path = message.command.path;
                this.$router.push(`${path}`);
                this.loading = false;

            } else {
                await this.submit(message).then((res) => {
                    if (res.data) {
                        this.formatMarkdown(JSON.stringify(res.data));
                    } else {
                        this.formatMarkdown(JSON.stringify(res));
                    }
                }).catch(error => {
                    if (error.response) {
                        this.onError(error);
                    } else {
                        this.loading = false;
                        var message = {
                            role: 'system',
                            text: error.response.data ? error.response.data.message : error
                        }
                        this.messages.push(message);
                    }
                })
            }
        },

        async submit(message) {
            var path = message.command.path;
            if (path.charAt(0) === '/') {
                path = path.substr(1);
            }
            var value = message.command.args;
            var repository = new BaseRepository(axios, path);

            if (message.command.method === 'POST') {
                return await repository.save(value, true);
            } else if (message.command.method === 'PUT') {
                return await repository.save(value, false);
            } else if (message.command.method === 'DELETE') {
                return await repository.delete(value);
            }
        },

        async formatMarkdown(value) {
            this.generator.previousMessages = [{
                role: 'system',
                content: `
You should only respond in JSON format as described below

RESPONSE FORMAT:
[{
    "markdown": "generated markdown text"
}]
                `
            }];
            this.newMessage = `
Generate the following values in markdown text format:
${value}
`;

            await this.generator.generate();

            this.newMessage = "";
        },

    }
}
</script>

<style scoped>
.chat-open-btn {
    position: fixed;
    z-index: 999;
    bottom: 15px;
    right: 15px;
}

.chatgpt-icon {
    width: 30px;
    height: 30px;
}
.chat-open-box {
    position: fixed;
    z-index: 999;
    bottom: 90px;
    right: 15px;
    width: 400px;
    max-width: 400px;
    height: 500px;
    max-height: 500px;
    padding: 5px;
}
.user-message {
    background: #9155FD;
    color: #ffffff;
    font-size: 16px;
    font-weight: bold;
    padding: 12px;
    border-radius: 20px;
}

.system-message {
    background: #eeeeee;
    font-size: 16px;
    font-weight: bold;
    padding: 12px;
    border-radius: 20px;
}

.message-box {
    overflow-y: auto;
    max-height: 435px;
}

.chat-box {
    position: absolute;
    bottom: 0px;
    right: 0px;
    width: 100%;
}

</style>