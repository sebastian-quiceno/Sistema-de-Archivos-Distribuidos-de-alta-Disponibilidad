# Implementación y configuración de interfaces e infraestructura

**Prioridad: Alta**

## Objetivo

Implementar, configurar y conectar correctamente todas las interfaces del proyecto, garantizando que los casos de uso dependan de abstracciones y que las implementaciones concretas se encuentren correctamente configuradas mediante inyección de dependencias.

El objetivo es completar la arquitectura de persistencia, seguridad y almacenamiento, respetando los principios SOLID y las convenciones existentes del proyecto. Utilizando el framework `Spring Boot`.

## 1. Repositorios

Implementar y configurar las siguientes interfaces:

text

```text
Repositories/
    IChunkRepository.java
    IFileRepository.java
    IFileTypeRepository.java
    IFolderRepository.java
    INodeRepository.java
    IUserRepository.java
```

### Tareas

- Revisar que la estructura de la base de datos (`metadata_DB/sdan.sql`) este acorde a las entidades del backed (``middleware/src/main/java/com/sdan/middleware/domain/model/entities/`) priorizando el backend, modificando la DB.
- Revisar cada interfaz e identificar los métodos necesarios para satisfacer las operaciones requeridas por los casos de uso existentes.
- Implementar los repositorios concretos en la capa de infraestructura, dentro de la siguiente ubicación:`middleware/src/main/java/com/sdan/middleware/Infraestructure/out`. Estos repositorios son la "implementacion" de los repositorios definidos en la capa de aplicacion (`middleware\src\main\java\com\sdan\middleware\Application\Ports\Repositories`)
- Garantizar que las implementaciones de infraestructura implementen los contratos definidos en la capa de aplicación, respetando la dirección de las dependencias de la arquitectura.
- Crear los procedimientos almacenados (_Stored Procedures_) necesarios para implementar las consultas y operaciones de persistencia de los repositorios.
- Definir dichos procedimientos almacenados en el archivo `metadata_DB/sdan.sql`, respetando las convenciones y estructura existentes.
- Utilizar la anotación `@Procedure` si JPA lo permite, sino utilizar `@Query` en los repositorios para invocar los procedimientos almacenados definidos en la base de datos.
- Garantizar que los parámetros enviados y los resultados recibidos mediante los procedimientos almacenados se correspondan correctamente con los métodos y modelos definidos en las interfaces.
- Garantizar que las consultas respeten las relaciones entre entidades, las restricciones de integridad y las reglas de persistencia del dominio.
- Evitar consultas innecesarias, problemas N+1 y recuperación de información que no sea requerida por las operaciones.
- Garantizar que las operaciones de persistencia respeten los tipos de datos, identificadores y relaciones definidos en el modelo de dominio.
- Implementar las operaciones de creación, consulta, actualización y eliminación que realmente necesiten los casos de uso, evitando métodos innecesarios.
- Evitar incorporar lógica de negocio dentro de los repositorios. Su responsabilidad debe limitarse al acceso y persistencia de datos.

### Criterios de aceptación

- Todas las interfaces solicitadas cuentan con sus respectivas implementaciones en la capa de infraestructura.
- Los procedimientos almacenados necesarios están definidos en `metadata_DB/sdan.sql`.
- Los repositorios deben utilizar `@Procedure` si JPA lo permite, sino deberan utilizar `@Query` para invocar los procedimientos almacenados correspondientes.
- Los parámetros, resultados y tipos de datos están correctamente mapeados.
- Las operaciones de persistencia funcionan correctamente y respetan las relaciones entre entidades.
- No se duplica lógica de negocio dentro de los repositorios.
- Los repositorios están correctamente integrados con los casos de uso existentes.


## 2\. Interfaces de seguridad

Implementar y configurar las siguientes interfaces:

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

### Tareas

- Revisar las responsabilidades de cada interfaz y su utilización dentro de los casos de uso.
- Implementar los mecanismos de autorización y consulta del usuario autenticado.
- Configurar correctamente la generación y validación de hashes de archivos.
- Configurar los mecanismos de hash y verificación de contraseñas utilizando algoritmos seguros y adecuados para contraseñas.
- Implementar la generación de tokens respetando los mecanismos de autenticación existentes.
- Configurar correctamente `Permissions` como parte del mecanismo de autorización, evitando duplicar reglas de acceso.
- Garantizar que los casos de uso no dependan directamente de implementaciones concretas de seguridad.
- No confundir el hash de integridad de archivos con el hash de contraseñas: deben utilizar estrategias y algoritmos apropiados para cada finalidad.

## 3\. Interfaces de almacenamiento

Implementar y configurar las siguientes interfaces:

text

```text
Storage/
    IFileDeletionService.java
    IFileIntegrityService.java
    IFileReconstructionService.java
    IFileUpdateService.java
    IStorageService.java
```

### Tareas

- Revisar las responsabilidades y operaciones que necesita cada interfaz.
- Implementar los servicios concretos correspondientes.
- Garantizar que cada servicio tenga una responsabilidad claramente definida.

Responsabilidades esperadas:

- **IFileDeletionService:** coordinar la eliminación de los chunks y sus respectivas réplicas.
- **IFileIntegrityService:** verificar la integridad de los archivos y gestionar los procesos de curación o recuperación cuando corresponda.
- **IFileReconstructionService:** reconstruir archivos a partir de sus chunks, respetando su orden y verificando su consistencia.
- **IFileUpdateService:** coordinar la actualización del contenido de un archivo, incluyendo el almacenamiento de nuevos chunks, la actualización de metadata y la eliminación de los antiguos.
- **IStorageService:** proporcionar las operaciones fundamentales de almacenamiento, recuperación y eliminación de contenido en los nodos.
- Reutilizar los servicios y mecanismos existentes de almacenamiento, replicación, hashing y reconstrucción cuando sean adecuados.
- Evitar duplicar lógica entre servicios.
- Garantizar una correcta gestión de streams y recursos.
- Contemplar los posibles fallos de comunicación con los nodos y las operaciones de almacenamiento.
- Evitar que los servicios de almacenamiento contengan responsabilidades propias de los casos de uso.

## 4\. Configuración de inyección de dependencias

### Tareas

- Configurar todas las implementaciones mediante el mecanismo de inyección de dependencias del framework.
- Garantizar que cada interfaz tenga una implementación concreta correctamente registrada.
- Revisar los constructores de los casos de uso y servicios para garantizar que dependan de interfaces y no de implementaciones concretas.
- Detectar y resolver dependencias circulares.
- Evitar el uso innecesario de `@Autowired` sobre atributos; priorizar la inyección por constructor.
- Revisar los scopes y ciclos de vida de los componentes cuando corresponda.
- Garantizar que la aplicación pueda iniciar correctamente con todas las dependencias configuradas.

## 5\. Integración con los casos de uso

Revisar los casos de uso existentes y garantizar que utilicen correctamente las interfaces implementadas.

Prestar especial atención a:

- `GetFileUseCase`
- `CheckFilesUseCase`
- `DeleteFileUseCase`
- `UpdateFileUseCase`

Verificar que:

- Los casos de uso dependan de abstracciones.
- Cada operación delegue sus responsabilidades en el servicio o repositorio correspondiente.
- No exista lógica de persistencia, almacenamiento o seguridad directamente implementada dentro de los casos de uso.
- No se dupliquen operaciones que ya estén disponibles en otros servicios.
- Se mantengan los contratos y comportamientos existentes.

## 6\. Validación y pruebas

### Tareas

- Crear o actualizar pruebas unitarias para las implementaciones de repositorios y servicios.
- Verificar las consultas personalizadas y sus resultados.
- Probar los mecanismos de autenticación, autorización y hashing.
- Verificar las operaciones de almacenamiento, eliminación, reconstrucción, integridad y actualización.
- Validar la inyección de dependencias mediante pruebas de contexto de la aplicación.
- Comprobar que no existan dependencias sin implementar ni errores de configuración.
- Ejecutar las pruebas existentes y corregir las regresiones detectadas.

## Criterios de aceptación

- Todas las interfaces solicitadas cuentan con implementaciones concretas cuando corresponde.
- Los repositorios ejecutan correctamente las consultas necesarias para los casos de uso.
- Las interfaces de seguridad y almacenamiento tienen responsabilidades claramente definidas.
- Todas las dependencias están correctamente registradas e inyectadas.
- Los casos de uso dependen de abstracciones y no de implementaciones concretas.
- No existen dependencias circulares ni implementaciones duplicadas innecesariamente.
- La aplicación inicia correctamente.
- Las pruebas unitarias y de integración relevantes pasan.
- Se respetan los principios SOLID, especialmente SRP, OCP y DIP.

## Consideraciones importantes

- Antes de crear nuevas implementaciones, revisar todo el código existente para identificar servicios, repositorios y componentes que puedan reutilizarse.
- No crear clases o interfaces adicionales sin una necesidad arquitectónica justificada.
- No modificar innecesariamente los contratos públicos de las interfaces existentes.
- No introducir lógica de negocio en los repositorios ni lógica de infraestructura en los casos de uso.
- Mantener las convenciones de nombres, paquetes y arquitectura del proyecto.
- Si alguna interfaz tiene responsabilidades incorrectamente definidas, documentar el problema y proponer el ajuste necesario antes de modificar su contrato.
- No considerar una interfaz implementada únicamente porque exista una clase que la implemente: verificar que su comportamiento sea correcto y que esté integrada con los casos de uso.

