package de.uni_stuttgart.tik.viplab.websocket_api.model;

import static org.junit.jupiter.api.Assertions.*;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

class ComputationResultTest {

  private static Jsonb jsonb;

  @BeforeAll
  static void setup() {
    JsonbConfig config = new JsonbConfig();
    jsonb = JsonbBuilder.create(config);
  }

  @Test
  void test() {
    String input = "{\"identifier\":\"86165eea-14df-4a76-805a-09b21441cbf7\",\"version\":\"3.0.0\",\"computation\":\"4dd3e271-5f1e-4905-9c43-cc88a1e26199\",\"status\":\"final\",\"timestamp\":\"2021-02-18T02:15:12+01:00\",\"output\":{\"stdout\":\"c29tZSByYW5kb20gc3Rkb3V0IGF0OiAyMDIxLTEwLTEzIGF0IDIwOjI1OjE4IEdNVA==\",\"stderr\":\"c29tZSByYW5kb20gc3RkZXJyIGF0OiAyMDIxLTEwLTEzIGF0IDIwOjI1OjE4IEdNVA==\"},\"artifacts\":[{\"type\":\"file\",\"identifier\":\"de762095-6cd2-439f-80eb-313e85d33869\",\"MIMEtype\":\"text/plain\",\"path\":\"/output/text1.txt\",\"content\":\"c29tZSBjb250ZW50\"},{\"type\":\"file\",\"identifier\":\"10516761-d937-4ba4-a82f-dc2847d45032\",\"MIMEtype\":\"text/plain\",\"path\":\"/output/text2.txt\",\"content\":\"c29tZSBvdGhlciBjb250ZW50\"}]}";
    ComputationResult cr = jsonb.fromJson(input,
            ComputationResult.class);
    assertEquals(cr.status,
            ComputationResult.STATUS.FINAL);
  }

}
