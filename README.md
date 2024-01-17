# discord-bot-dodo

## 初期設定

### GCP クレデンシャル設定

[spring-cloud-gcp](https://googlecloudplatform.github.io/spring-cloud-gcp/reference/html/index.html#credentials)
の説明を参考にクレデンシャル設定を行う。

### Discord 設定

discord 用の下記環境変数を設定する。

- `DISCORD_TOKEN`

### Azure 設定

azure 用の下記環境変数を設定する。

- `AZURE_SPEECH_KEY`
- `AZURE_SPEECH_REGION`

## build

## deploy

```
gcloud compute scp C:\Users\kim_m\IdeaProjects\discord-bot-dodo\build\libs\discord-bot-dodo-1.1.0.jar roh-dodo:discord-bot-dodo.jar
```

```
sudo mv /home/kim_m/discord-bot-dodo.jar /home/kimuchidev/dodo/bot-java/
sudo systemctl restart dodo-bot
sudo systemctl status dodo-bot
```