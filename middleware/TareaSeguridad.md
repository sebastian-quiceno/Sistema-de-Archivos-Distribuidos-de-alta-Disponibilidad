## 2\. Implementación e integración de Spring Security

**Prioridad: Alta**

### Objetivo

Implementar y configurar Spring Security como mecanismo de autenticación y autorización del proyecto, integrándolo con la arquitectura existente y garantizando que los casos de uso puedan acceder de forma segura a la identidad y los permisos del usuario autenticado.

Además, implementar las interfaces de seguridad definidas en la capa de aplicación, utilizando las implementaciones correspondientes en infraestructura.

### Interfaces involucradas

text

```text
Security/
    IAuthorizationService.java
    ICurrentUser.java
    IFileHasher.java
    IPasswordHasher.java
    ITokenGenerator.java
    Permissions.java
```

### 1. Configuración de Spring Security

- Analizar la configuración actual de autenticación y autorización del proyecto antes de implementar Spring Security.
- Configurar Spring Security como mecanismo central de seguridad de la aplicación.
- Definir correctamente la `SecurityFilterChain` y los filtros necesarios para el proceso de autenticación.
- Configurar las reglas de acceso a los endpoints, diferenciando las rutas públicas de las que requieren autenticación.
- Garantizar que las solicitudes protegidas sean autenticadas antes de llegar a los controladores y casos de uso.
- Configurar el manejo de errores de autenticación y autorización, utilizando respuestas HTTP apropiadas (`401 Unauthorized` y `403 Forbidden`).
- Evitar configuraciones de seguridad duplicadas o mecanismos de autenticación independientes que puedan generar inconsistencias.

### 2. Autenticación mediante tokens

- Revisar el mecanismo de autenticación existente y determinar cómo integrarlo con Spring Security.
- Implementar la generación y validación de tokens mediante `ITokenGenerator`.
- Utilizar JWT, configurar correctamente:
  - Generación de tokens.
  - Validación de firma.
  - Validación de expiración.
  - Validación de claims necesarios.
  - Extracción de la identidad y permisos del usuario.
- Implementar un filtro de autenticación que permita obtener el token de las solicitudes entrantes, validarlo y establecer el contexto de seguridad de Spring.
- Garantizar que las solicitudes autenticadas establezcan correctamente el `SecurityContext`.
- Configurar una estrategia de autenticación sin estado (_stateless_) utilizando JWT.
- No confiar en información de identidad o permisos proporcionada directamente por el cliente sin verificar su autenticidad.

### 3. Implementación de las interfaces de seguridad

Revisar e implementar las siguientes interfaces respetando sus responsabilidades:

- **IAuthorizationService:** centralizar las comprobaciones de autorización y permisos del usuario.
- **ICurrentUser:** proporcionar acceso a la identidad del usuario autenticado, evitando que los casos de uso dependan directamente de `SecurityContextHolder` o de clases de Spring Security.
- **IFileHasher:** generar y verificar hashes de archivos para garantizar su integridad.
- **IPasswordHasher:** generar y verificar hashes de contraseñas utilizando algoritmos especializados y seguros para almacenamiento de contraseñas.
- **ITokenGenerator:** encargarse de generar los tokens de autenticación y proporcionar los mecanismos necesarios para su validación, según el contrato definido.
- **Permissions:** centralizar la definición de permisos y evitar duplicar reglas de autorización en diferentes componentes.

Las implementaciones concretas deben ubicarse en la capa de infraestructura y registrarse correctamente mediante inyección de dependencias.

### 4. Integración con usuarios y autorización

- Integrar Spring Security con el mecanismo de persistencia de usuarios existente.
- Configurar la carga de usuarios y sus credenciales mediante el mecanismo correspondiente de Spring Security, como `UserDetailsService`, cuando aplique.
- Garantizar que la identidad obtenida durante la autenticación corresponda a un usuario existente y válido.
- Configurar correctamente la conversión de roles y permisos del usuario a las autoridades de Spring Security.
- Integrar las comprobaciones de autorización con `IAuthorizationService` e `ICurrentUser`.
- Garantizar que los casos de uso puedan identificar al usuario autenticado sin recibir su identificador como un dato confiable proporcionado por el cliente.
- Evitar duplicar las reglas de autorización entre los controladores, filtros y casos de uso.

### 5. Seguridad de contraseñas y archivos

- Utilizar algoritmos especializados para contraseñas, como BCrypt o Argon2, mediante las implementaciones correspondientes de `IPasswordHasher`.
- Nunca almacenar contraseñas en texto plano ni utilizar hashes rápidos como SHA-256 para almacenar contraseñas directamente.
- Configurar `IFileHasher` con un algoritmo apropiado para la verificación de integridad de archivos, respetando los mecanismos existentes de hashing del proyecto.
- Garantizar que los algoritmos y responsabilidades de hashing de contraseñas y archivos permanezcan completamente separados.

### 6. Integración con la arquitectura existente

- Garantizar que los casos de uso dependan de las interfaces de seguridad y no de implementaciones concretas de Spring Security.
- Mantener las dependencias de Spring Security dentro de la capa de infraestructura.
- Configurar correctamente la inyección de dependencias de todas las implementaciones.
- Revisar los casos de uso existentes para garantizar que las validaciones de propiedad y autorización utilicen los mecanismos centralizados.
- Evitar dependencias circulares y duplicación de lógica de seguridad.
- Respetar los principios SOLID, especialmente SRP y DIP.

### 7. Pruebas y validación

- Implementar pruebas unitarias para las implementaciones de las interfaces de seguridad.
- Verificar la generación y validación de tokens.
- Probar tokens válidos, expirados, inválidos y manipulados.
- Verificar la autenticación de usuarios y la carga de sus permisos.
- Comprobar que los endpoints públicos y protegidos tengan el comportamiento esperado.
- Validar las respuestas `401` y `403` en los escenarios correspondientes.
- Verificar que un usuario no pueda acceder a recursos pertenecientes a otro usuario.
- Comprobar que los casos de uso obtengan correctamente la identidad del usuario autenticado.
- Ejecutar las pruebas existentes y corregir cualquier regresión introducida.

## Criterios de aceptación

- Spring Security está correctamente implementado y configurado en el proyecto.
- Las rutas públicas y protegidas tienen reglas de acceso correctamente definidas.
- La autenticación mediante tokens funciona correctamente.
- Los tokens se validan antes de permitir el acceso a recursos protegidos.
- La identidad y los permisos del usuario se establecen correctamente en el contexto de seguridad.
- Todas las interfaces de seguridad solicitadas cuentan con implementaciones funcionales.
- Los casos de uso dependen de abstracciones y no de clases concretas de Spring Security.
- Las contraseñas y los hashes de archivos utilizan mecanismos independientes y apropiados.
- La autorización y validación de propiedad funcionan correctamente.
- La aplicación gestiona adecuadamente los errores de autenticación y autorización.
- Las pruebas unitarias y de integración relevantes pasan.

## Consideraciones importantes

- Antes de implementar, analizar la autenticación existente y reutilizar los componentes que sigan siendo adecuados.
- No acoplar la capa de aplicación a Spring Security.
- No confiar únicamente en la protección de endpoints: las reglas de propiedad y autorización de recursos también deben cumplirse dentro de los casos de uso.
- No introducir configuraciones de seguridad permisivas para facilitar las pruebas.
- Priorizar la seguridad y mantener una configuración coherente entre todos los componentes.
