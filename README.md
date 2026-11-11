# Site do CACo — backend

Backend da aplicação do Centro Acadêmico da Computação, desenvolvido com Spring Boot, Maven e Java 21.

## Arquitetura

O backend segue uma arquitetura modular em camadas, organizada por domínio:

- `controller`: endpoints REST e tratamento das requisições;
- `service`: regras de negócio;
- `repository`: acesso aos dados;
- `entity`: entidades persistidas pelo JPA;
- `dto/request` e `dto/response`: objetos de entrada e saída da API.

Esse projeto não utiliza o padrão MVVM (Model-View-ViewModel), pois é um backend REST e não possui uma camada de View ou ViewModel.

## Configuração no IntelliJ IDEA

### Pré-requisitos

- IntelliJ IDEA com suporte a Java e Maven;
- JDK 21 instalado e configurado;
- Git.

### Importar o projeto

1. Clone o repositório e abra a pasta do projeto no IntelliJ:

   ```bash
   git clone https://github.com/cacounicamp/site-do-caco-backend
   cd site-do-caco-backend
   ```

2. No IntelliJ, abra o arquivo `sitedocaco/pom.xml` ou a pasta `sitedocaco` como um projeto Maven.
3. Aguarde o IntelliJ baixar as dependências do Maven.
4. Em **File > Project Structure > Project**, selecione o **SDK 21**.
5. Em **Settings > Build, Execution, Deployment > Build Tools > Maven**, confirme que o Maven está usando o JDK 21.

### Configurar a execução local

1. Abra a classe `sitedocaco/src/main/java/com/caco/sitedocaco/SitedocacoApplication.java`.
2. Clique com o botão direito no método `main` e selecione **Run 'SitedocacoApplication'**. O IntelliJ criará uma configuração automaticamente.
3. Abra **Run > Edit Configurations...**, selecione essa configuração e defina:
   - **Working directory:** `<raiz-do-repositório>/sitedocaco`
   - **Use classpath of module:** `sitedocaco`
   - **VM options:** `-Dspring.profiles.active=local`
4. Salve e execute a configuração pelo botão **Run**.

O perfil `local` usa `application-local.properties`, inicia um WireMock para simular o OAuth2 e configura um banco SQLite em `sitedocaco/data/sqlite.db`. A pasta e o arquivo do banco são criados automaticamente na primeira execução.

Ao iniciar, a API estará disponível em:

```text
http://localhost:8080/api
```

O mock OAuth2 local escuta em `http://localhost:9988`.

### Configurações opcionais

Antes de executar, edite `sitedocaco/src/main/resources/application-local.properties` caso precise:

- preencher `jwt.secret` para testar autenticação por JWT;
- preencher `imgbb.api.key` para testar upload de imagens;
- alterar `app.frontend.url` para apontar para o frontend local.

Não coloque chaves reais no repositório. Para desenvolvimento com MySQL ou OAuth2 real, use `application.properties.example` como referência e mantenha os valores somente na configuração local.

## Executar pelo Maven

Também é possível iniciar o projeto pelo terminal, a partir da pasta `sitedocaco`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Contribuições do projeto

Para contribuir:

1. Verifique nas **Issues** se a proposta já foi registrada. Se ela existir, comente informando que pretende trabalhar nela e aguarde a confirmação ou combine a colaboração com quem já estiver responsável. Se não existir, crie uma Issue descrevendo a proposta antes de começar.
2. Crie um **Fork** do repositório e use a branch `development` como base.
3. Faça as alterações necessárias no código, seguindo os padrões já adotados no projeto.
4. Abra um **Pull Request** do seu fork para a branch `development` do repositório original, incluindo uma breve descrição do que foi alterado e indicando a Issue relacionada.


## Uso de Large Language Models (LLMs)

Para manter o código legível e eficiente, todo código gerado por LLMs, como Codex, Claude, Gemini ou Copilot, deve ser revisado antes de ser incluído em um commit.
