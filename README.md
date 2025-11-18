# NewsApp

**NewsApp** é uma aplicação Android nativa moderna para leitura de notícias, desenvolvida como parte da disciplina de Desenvolvimento para Dispositivos Móveis. O aplicativo consome a API GNews para fornecer manchetes em tempo real, destaques de negócios e notícias regionais, além de permitir pesquisas personalizadas.

---

## Funcionalidades

* **Navegação Principal:** Barra inferior com acesso rápido a notícias Recentes (Geral), Destaques (Negócios) e Regionais (Nação).
* **Explorar Categorias:** Ecrã dedicado para filtrar notícias por tópicos como Tecnologia, Desporto, Ciência, Saúde, Entretenimento, entre outros.
* **Pesquisa Inteligente:** Barra de busca com *debounce* (atraso) para otimizar requisições à API enquanto o utilizador digita.
* **Leitura Completa:** Integração com `WebView` para ler o artigo original completo sem sair da aplicação.
* **Interface Moderna:** UI construída 100% em Jetpack Compose seguindo diretrizes do Material Design 3.

## Tecnologias e Bibliotecas

O projeto segue a arquitetura **MVVM (Model-View-ViewModel)** e utiliza as seguintes tecnologias:

* **Linguagem:** Kotlin
* **UI:** Jetpack Compose (Material3)
* **Navegação:** Navigation Compose
* **Networking:** Retrofit 2 & Gson Converter
* **Imagens:** Coil (Carregamento assíncrono de imagens)
* **Assincronismo:** Coroutines & Kotlin Flow
* **API Externa:** [GNews API](https://gnews.io/)

## Como Executar

### Pré-requisitos
* Android Studio Ladybug ou superior.
* JDK 11 ou superior configurado no Gradle.
* Uma chave de API gratuita do **GNews**.

### Configuração da API Key
Por questões de segurança, a chave da API não está incluída no controlo de versão. Para executar o projeto:

1.  Obtenha uma chave gratuita em [gnews.io](https://gnews.io/).
2.  Abra o ficheiro `local.properties` na raiz do projeto (se não existir, crie-o).
3.  Adicione a seguinte linha:

```properties
NEWS_API_KEY=SUA_CHAVE_AQUI
```

4. Sincronize o projeto com o Gradle (Sync Project with Gradle Files). O BuildConfig irá gerar a variável automaticamente.

## Sobre o Projeto

  Este projeto foi desenvolvido como parte da avaliação da disciplina de **Desenvolvimento para Dispositivos Móveis (2025/2).**
  
  Instituição: UDESC Alto Vale
  
  Professor: Mattheus da Hora França
  
  Requisitos Atendidos:
  
  [x] Uso de componentes de lista (LazyColumn).
  
  [x] Navegação entre telas (Navigation Compose).
  
  [x] Integração com API Externa (Retrofit).
  
  [x] Arquitetura MVVM.
  
  Desenvolvido por Leonardo Gaertner e Fernando Prim
