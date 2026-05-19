package com.wj.future.campus.service;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.UserMessage;

public interface AiImageService {

    Response<Image> generateImage(@UserMessage String msg);
}
