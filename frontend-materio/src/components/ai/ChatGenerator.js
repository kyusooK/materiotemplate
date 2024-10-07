import JsonAIGenerator from "./JsonAIGenerator";

export default class ChatGenerator extends JsonAIGenerator{

    constructor(client, language){
        super(client, language);
    }

    createPrompt(){
       return this.client.newMessage
    }

}