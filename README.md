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

## Compilar

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

O Gradle fixa a JVM do próprio daemon em `gradle/gradle-daemon-jvm.properties`,
então não é preciso configurar `JAVA_HOME`.

## Licença

Domínio público, sob a [Unlicense](UNLICENSE). As marcas citadas acima não são
abrangidas por essa dedicação: elas pertencem a seus titulares.
