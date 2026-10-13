# LotoLivre

Resultados das loterias federais brasileiras, sem anúncios e sem rastreamento.

LotoLivre mostra o resultado mais recente de cada modalidade numa única lista e
deixa navegar concurso a concurso. Nada além disso: não gera jogos, não guarda
apostas, não calcula estatísticas e não pede nenhum dado seu. A única permissão
que o aplicativo declara é `INTERNET`.

## Não é um aplicativo oficial

**LotoLivre não é um aplicativo oficial da Caixa Econômica Federal**, não é
afiliado, patrocinado ou endossado por ela, e não tem qualquer ligação com a
instituição. Nunca peça nem informe senhas, dados bancários ou números de
apostas aqui — o aplicativo não os solicita em nenhuma tela.

Mega-Sena, Lotofácil, Quina, Lotomania, Dupla Sena, +Milionária, Timemania, Dia
de Sorte, Super Sete, Loteca e Federal são marcas da Caixa Econômica Federal,
usadas aqui de forma descritiva apenas para nomear os resultados que rotulam.
Todos os direitos sobre elas permanecem de seus respectivos titulares. Nenhum
logotipo ou elemento da identidade visual das Loterias Caixa é distribuído com
este aplicativo.

Os resultados são dados públicos, obtidos da API do portal de loterias da Caixa.
Essa API não é documentada e pode mudar sem aviso.

Os resultados já consultados ficam em cache; concursos anteriores são lidos
primeiro desse cache. Não há consulta por temporizador. Ao voltar ao aplicativo,
a lista é reconsiderada se a última verificação tem pelo menos cinco minutos.
Uma modalidade com resultado em cache e próximo sorteio ainda no futuro não é
consultada automaticamente. Puxar a lista permite conferir esse resultado mesmo
antes da data prevista, mas nenhuma modalidade é consultada duas vezes dentro
de um minuto, inclusive quando a tentativa anterior falhou.

## Compilar

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

O Gradle fixa a JVM do próprio daemon em `gradle/gradle-daemon-jvm.properties`,
então não é preciso configurar `JAVA_HOME`.

Para gerar um APK de release, crie `release-signing.properties` na raiz do
projeto, com `storeFile`, `storePassword`, `keyAlias` e `keyPassword`. `storeFile`
indica uma chave privada existente; caminhos relativos são resolvidos a partir
da raiz do projeto. O arquivo de propriedades e arquivos `*.jks` e `*.keystore`
são ignorados pelo Git. Mantenha a chave e sua cópia de segurança fora do
repositório.

```sh
./gradlew :app:assembleRelease
```

O build de release exige esses dados de assinatura e falha se eles estiverem
ausentes ou inválidos. Ele não usa a chave de debug. Os builds de debug e os
testes de unidade não precisam desse arquivo.

## Licença

Domínio público, sob a [Unlicense](UNLICENSE). As marcas citadas acima não são
abrangidas por essa dedicação: elas pertencem a seus titulares.
