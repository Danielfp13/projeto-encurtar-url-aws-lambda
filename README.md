# AWS URL Shortener
Este projeto é um encurtador de URL baseado em AWS, que utiliza funções Lambda, o S3 para armazenamento e o API Gateway da AWS para padronizar a URL de acesso nas duas Lambdas. Ele permite criar URLs encurtadas que redirecionam para os URLs originais.

## Visão Geral

### Funcionamento

1. **Criação de URL Encurtada:**
   - Recebe uma URL fornecida pelo usuário via API Gateway.
   - Gera um código único (UUID de 8 dígitos).
   - Salva a URL original e o tempo de expiração em um arquivo JSON no S3.
   - Retorna uma URL encurtada padronizada.

2. **Redirecionamento de URL:**
   - Recebe o código curto da URL via API Gateway.
   - Recupera os dados do S3.
   - Redireciona para a URL original se a URL não estiver expirada.

---

## Estrutura do Projeto

- **Lambda 1: Criar URL Encurtada**
  - Recebe um JSON com a URL original.
  - Gera um código UUID único de 8 dígitos.
  - Salva o seguinte conteúdo no S3:
    - Nome do arquivo: `<uuid>.json` (exemplo: `b616f106.json`).
    - Conteúdo do arquivo:
      ```json
      {
        "originalUrl": "https://exemplo.com",
        "expirationTime": 1732390697
      }
      ```
  - Retorna a URL encurtada no formato: `https://<base-url>/<uuid>`.
  - O `<base-url>` é gerado automaticamente pelo API Gateway, por exemplo: `https://a7b19lu782.execute-api.us-east-1.amazonaws.com`.

- **Lambda 2: Redirecionar URL**
  - Recebe o código curto via URL.
  - Busca os dados correspondentes no S3.
  - Verifica se a URL expirou:
    - Se sim, retorna status **410 (Gone)**.
    - Caso contrário, redireciona para a URL original com status **302 (Found)**.

- **API Gateway**
  - Padroniza as URLs de acesso para as funções Lambda:
    - **POST** `/create` → Chama a Lambda de criação.
    - **GET** `/{uuid}` → Chama a Lambda de redirecionamento.

---

## Configuração

### Variáveis de Ambiente

Configure as seguintes variáveis de ambiente para ambas as Lambdas:

- `BUCKET_NAME`: Nome do bucket S3 para armazenar os dados das URLs.
- `BASE_URL`: URL padronizada do API Gateway da AWS, que será usada como base para gerar as URLs encurtadas.
- **Lambda de Criação:**
  - `EXPIRATION_TIME_SECONDS`: Tempo de expiração em segundos (exemplo: `604800` para 7 dias).

### Deploy

1. **Criar o Bucket S3:**
   - Acesse o Console AWS e crie um bucket no S3 para armazenar os dados das URLs encurtadas.
   - Este bucket será utilizado pelas funções Lambda para armazenar e recuperar os arquivos JSON contendo as URLs originais e os dados associados.

2. **Criar as Funções Lambda:**
   - Crie duas funções Lambda:
     - **Lambda 1 (Criar URL Encurtada):**
       - Responsável por gerar o código UUID, salvar a URL original no S3 e retornar a URL encurtada.
     - **Lambda 2 (Redirecionar URL):**
       - Responsável por pegar o código UUID da URL, buscar os dados no S3 e redirecionar para a URL original.

3. **Conceder Permissões para Acessar o S3:**
   - Conceda permissões às Lambdas para acessarem o bucket no AWS S3.

4. **Empacotar o Código das Lambdas:**
   - Execute o comando abaixo para empacotar o código das Lambdas:
     ```bash
     mvn clean package
     ```
   - O comando irá gerar o arquivo `.jar` que será usado para o deploy.

5. **Fazer o Upload do Arquivo JAR para as Funções Lambda:**
   - Acesse o Console AWS e faça o upload do arquivo `.jar` gerado para as funções Lambda.
   - Certifique-se de que as variáveis de ambiente e permissões estejam configuradas corretamente.

6. **Criar e Configurar o API Gateway:**
   - Crie uma API REST no API Gateway para expor as funções Lambda.
   - Configure os seguintes endpoints:
     - **POST /create** → Lambda de criação da URL encurtada.
     - **GET /{uuid}** → Lambda de redirecionamento para a URL original.
   - O API Gateway irá fornecer a URL base (BASE_URL) que será usada para gerar as URLs encurtadas.

7. **Configuração da URL Base (BASE_URL):**
   - Após configurar os recursos e métodos no API Gateway, copie a URL base fornecida. Ela será usada para criar URLs encurtadas, por exemplo:
     ```text
     https://<api-id>.execute-api.<region>.amazonaws.com
     ```
   - Essa URL será combinada com o código UUID gerado para formar as URLs encurtadas.
