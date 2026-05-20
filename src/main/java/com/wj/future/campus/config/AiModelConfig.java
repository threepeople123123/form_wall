package com.wj.future.campus.config;

import com.wj.future.campus.aiTools.AllTools;
import com.wj.future.campus.handler.ChatMemoryStoreHandler;
import com.wj.future.campus.properties.ApiKeyProperties;
import com.wj.future.campus.rag.TypesenseVectorContentRetriever;
import com.wj.future.campus.service.AiSimplifyModelService;
import com.wj.future.campus.service.AiStreamService;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiModelConfig {

    @Autowired
    ApiKeyProperties apiKeyProperties;

    @Autowired
    private ChatMemoryStoreHandler chatMemoryStoreHandler;

    @Autowired
    private TypesenseVectorContentRetriever typesenseVectorContentRetriever;

    @Autowired
    private AllTools allTools;


    @Bean("openAiStreamingChatModel")
    public AiStreamService openAiStreamingChatModel() {
        OpenAiStreamingChatModel openAiStreamingChatModel = OpenAiStreamingChatModel.builder()
                .baseUrl(apiKeyProperties.getGateway().getUrl())
                .apiKey(apiKeyProperties.getGateway().getLitellmMasterKey())
                .modelName(apiKeyProperties.getDashscope().getQwen3_5_122b_a10b()).build();

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(chatMemoryStoreHandler)
                .build();

        return AiServices.builder(AiStreamService.class)
                .streamingChatModel(openAiStreamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .contentRetriever(typesenseVectorContentRetriever)  // 添加向量检索器
                .tools(allTools.getAllTools().toArray())  // 自动注册所有工具
                .systemMessage("""
                        你是一个校园文章助手，可以帮助用户搜索和查找文章，需要使用俚语进行回答。
                        重要规则:
                        1. 当用户询问关于文章、帖子、内容相关问题时，优先调用工具，查询系统内部的文章
                        2. 根据搜索结果，用自然语言总结并回答用户的问题
                        3. 如果搜索结果为空，可以告诉用户系统中没有找到，然后去查询网上的相关文章
                        4. 不要编造不存在的文章内容
                        """)
                .build();
    }

    @Bean
    public OpenAiImageModel openAiImageModel() {
        return OpenAiImageModel.builder()
                .baseUrl(apiKeyProperties.getGateway().getUrl())
                .apiKey(apiKeyProperties.getGateway().getLitellmMasterKey())
                .modelName(apiKeyProperties.getDashscope().getDashscope_qwen_image_2_0()).build();
    }


    @Bean
    public AiSimplifyModelService aiSimplifyModelService() {
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .baseUrl(apiKeyProperties.getGateway().getUrl())
                .apiKey(apiKeyProperties.getGateway().getLitellmMasterKey())
                .modelName(apiKeyProperties.getDashscope().getQwen3_5_122b_a10b()).build();


        return AiServices.builder(AiSimplifyModelService.class)
                .chatModel(openAiChatModel)
                .systemMessage("""
                        你是一名“对话总结助手”。
                               任务要求：
                               1，读取大模型输出的话语。
                               2，提炼核心意思，压缩冗余表达。
                               3，保留关键事实、诉求、情绪和结论。
                               4，使用简洁自然的一句话或几句话输出。
                               5，不添加解释、不分析、不回复用户、不扩展内容。
                               6，只返回总结后的内容。
                               7，禁止出现“总结：”“用户意思是”等前缀。
                               8，禁止输出与总结无关的任何文字。
                               9，总结的话术尽量简短
                               
                        例如：
                        
                        用户输入：npm error code EPERM npm error syscall mkdir npm error path C:\\software\\office\\software\\nvm\\v18.20.5\\node-v24.14.0-win-x64\\node_modules\\opencode-ai npm error errno -4048 npm error Error: EPERM: operation not permitted, mkdir 'C:\\software\\office\\software\\nvm\\v18.20.5\\node-v24.14.0-win-x64\\node_modules\\opencode-ai' npm error at async mkdir (node:internal/fs/promises:856:10) npm error at async C:\\software\\office\\software\\nvm\\v18.20.5\\node-v24.14.0-win-x64\\node_modules\\npm\\node_modules\\@npmcli\\arborist\\lib\\arborist\\reify.js:572:20 npm error at async Promise.allSettled (index 0) npm error at async #reifyPackages (C:\\software\\office\\software\\nvm\\v18.20.5\\node-v24.14.0-win-x64\\node_modules\\npm\\node_modules\\@npmcli\\arborist\\lib\\arborist\\reify.js:309:11) npm error at async Arborist.reify (C:\\software\\office\\software\\nvm\\v18.20.5\\node-v24.14.0-win-x64\\node_modules\\npm\\node_modules\\@npmcli\\arborist\\lib\\arborist\\reify.js:121:5) npm error at async Install.exec (C:\\software\\office\\software\\nvm\\v18.20.5\\node-v24.14.0-win-x64\\node_modules\\npm\\lib\\commands\\install.js:151:5) npm error at async Npm.exec (C:\\software\\office\\software\\nvm\\v18.20.5\\node-v24.14.0-win-x64\\node_modules\\npm\\lib\\npm.js:208:9) npm error at async module.exports (C:\\software\\office\\software\\nvm\\v18.20.5\\node-v24.14.0-win-x64\\node_modules\\npm\\lib\\cli\\entry.js:67:5) { npm error errno: -4048, npm error code: 'EPERM', npm error syscall: 'mkdir', npm error path: 'C:\\\\software\\\\office\\\\software\\\\nvm\\\\v18.20.5\\\\node-v24.14.0-win-x64\\\\node_modules\\\\opencode-ai' npm error } npm error npm error The operation was rejected by your operating system. npm error It's possible that the file was already in use (by a text editor or antivirus), npm error or that you lack permissions to access it. npm error npm error If you believe this might be a permissions issue, please double-check the npm error permissions of the file and its containing directories, or try running npm error the command again as root/Administrator. npm error A complete log of this run can be found in: C:\\Users\\wj\\AppData\\Local\\npm-cache\\_logs\\2026-05-18T11_37_36_011Z-debug-0.log
                        
                        大模型回答：
                                这是典型的 Windows 权限/目录占用问题，但你这里还有一个更明显的问题：
                              你的路径里出现了：
                              ...nvm\\v18.20.5\\node-v24.14.0-win-x64\\node_modules\\
                              这说明你的 Node 安装目录结构已经有点异常了。
                              正常 nvm-windows 不应该在 v18.20.5 下面再嵌套一个 node-v24.14.0-win-x64。
                              大概率是：
                              手动解压过 Node
                              或 npm prefix 配错
                              或 nvm 安装损坏
                              或权限继承异常
                              先按下面顺序排查。
                              1. 先看你当前 npm 全局目录
                              执行：
                              npm config get prefix
                              如果输出：
                              C:\\software\\office\\software\\nvm\\v18.20.5\\node-v24.14.0-win-x64
                              那就是配置错了。
                              正常应该类似：
                              C:\\software\\office\\software\\nvm\\v18.20.5
                              或者：
                              C:\\Program Files\\nodejs
                              2. 修复 npm prefix（最重要）
                              执行：
                              npm config delete prefix
                              然后重新打开终端。
                              再执行：
                              npm config get prefix
                              看看是否恢复正常。
                              3. 删除异常目录
                              你这个目录：
                              C:\\software\\office\\software\\nvm\\v18.20.5\\node-v24.14.0-win-x64
                              建议直接删掉。
                              如果删不掉：
                              用管理员 PowerShell
                              执行：
                              Remove-Item -Recurse -Force "C:\\software\\office\\software\\nvm\\v18.20.5\\node-v24.14.0-win-x64"
                              4. 检查是否被占用
                              Windows 下最常见：
                              VSCode 正在索引
                              杀毒软件锁目录
                              node 进程未退出
                              执行：
                              taskkill /F /IM node.exe
                              再试。
                              5. 不要在 nvm 的 node 安装目录里装全局包
                              你现在像是在：
                              nvm\\v18.20.5\\
                              内部直接 npm install。
                              这是非常容易炸权限的。
                              建议：
                              正确做法
                              全局：
                              npm install -g opencode-ai
                              项目：
                              cd 你的项目目录
                              npm install opencode-ai
                              不要进 node 安装目录。
                              6. 如果还是不行：重建 nvm（Windows 很常见）
                              nvm-windows 经常会出现目录权限污染。
                              最快的解决方案：
                              删除：
                              C:\\software\\office\\software\\nvm
                              重新安装：
                              nvm-windows
                              node
                              然后：
                              nvm install 18.20.5
                              nvm use 18.20.5
                              7. 最后验证
                              执行：
                              where node
                              where npm
                              node -v
                              npm -v
                              npm config get prefix
                              正常应该：
                              node 指向 nvm 当前版本
                              npm prefix 不再包含 node-v24.xxx
                              你这个问题本质上不是 opencode-ai 的问题，而是：
                              npm 的全局安装目录已经错乱了。
                              
                        最终总结的标题：npm EPERM 权限问题
                        """)
                .build();
    }

}
