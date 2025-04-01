# DashX SDK for Java

## Usage (Spring Boot)
Add the following to your `build.gradle.kts` file:

```
implementation("com.dashx:dashx-java:1.0.0")
```

In most cases, you might just need one instance of DashX and you can create a [Bean](https://docs.spring.io/spring-framework/reference/core/beans/java/bean-annotation.html) in your Spring Boot application to make it available in your controllers.

Please refer to [DashXDemoSpringBootApplication.kt](dashx-demo-spring-boot/src/main/kotlin/com/dashx/demo/springboot/DashXDemoSpringBootApplication.kt) in the demo application for how to set it up. And refer to [HelloController.kt](dashx-demo-spring-boot/src/main/kotlin/com/dashx/demo/springboot/HelloController.kt) in the demo application for how to use the `dashx` bean.

## Development

### Prerequisites

#### Java
```
brew install java
```

#### SDKMAN
```
curl -s "https://get.sdkman.io" | bash
```

#### Gradle
```
sdk install gradle 8.12
```

This project uses gradle wrapper, to initialize it, run:
```
gradle wrapper
```

### Running gradle tasks
You can build the project using:
```
gradle build
```

To do a clean build, run:
```
gradle clean
```
before the build command.

To start the demo application, run:
```
gradle dashx-demo-spring-boot:bootRun
```

Now you should be able to see the greeting message on `http://localhost:8080`.

To see all the available gradle commands, run:
```
gradle -q :tasks --all
```

## Publishing

You will need `gnupg` to generate the signing keys. If you don't have it installed, please run:
```
brew install gnupg gnupg2
```

#### Generate a new GPG key
```
gpg --full-generate-key
```
- Select “RSA and RSA” (default)
- Enter key size → 4096 (recommended for security)
- Set expiration → Choose 1y (1 year) or 0 (no expiration)
- Enter name & email → Should match your Sonatype account
- Set a strong passphrase

#### Distribute your public key

First, list your gpg keys using:
```
gpg --list-keys --keyid-format LONG
```

You should see something like this:
```
pub   rsa4096/3AA5C34371567BD2 2024-01-01 [SC]
....
```

`3AA5C34371567BD2` is your GPG key id.

Since you'll be using this key to sign the release files, other people would need your public key to verify the signature. To distribute your public key, please run:
```
gpg --keyserver keyserver.ubuntu.com --send-keys 3AA5C34371567BD2
```

You can find more information regarding this [here](https://central.sonatype.org/publish/requirements/gpg/#distributing-your-public-key).

#### Start the GPG Agent Daemon
```
gpg-agent --daemon
```

In order for GPG agent to work properly, you might need to put the following in your `~/.zshrc` file:
```
export GPG_TTY=$(tty)
```

Don't forget to run `source ~/.zshrc` after updating your `~/.zshrc` file.

#### Setup publishing credentials
To publish to Maven Central repository, you will first need to generate the user token in your Sonatype account. You can do so by clicking on the **User** icon in the top right corner -> View Account -> Generate User Token.

You can then supply credentials to the publishing task by setting them in `~/.gradle/gradle.properties` file. You might already have `~/.gradle` folder but no `gradle.properties` file in it. If that's the case, please go ahead and create this file and set the following values in it:
```
mavenCentralUsername=GENERATED_TOKEN_USERNAME
mavenCentralPassword=GENERATED_TOKEN_PASSWORD

signing.keyId=GPG_KEY_ID
signing.password=GPG_KEY_PASSPHRASE
```

#### Publishing a new version
Please make sure you've set the new version number in the [build file](dashx/build.gradle.kts) before publishing.

To publish the new version, run:
```
gradle publish
```

If all goes correctly, you should be able to see a new deployment in your [Sonatype account](https://central.sonatype.com/publishing/deployments).
