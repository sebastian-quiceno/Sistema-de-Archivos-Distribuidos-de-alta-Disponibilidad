# NOTAS A TENER EN CUENTA

## [ ] 1. Implementar Seguridad por medio de Spring Security

> [!CAUTION]
> Es un requisito del documento, revisar si solo validar autentication o tambien authorization

- `Solucion:` **Pendiente**

## [ ] 2. Implementar Permisos

> [!TIP]
> No es critico, ni creo que sea importante, pero se puede implementar

- `Solucion:` **Pendiente**

## [x] ~~3. Envio Archivos~~

> [!IMPORTANT]
> El archivo lo envia el usuario (FrontEnd) al servidor (Backend), el servidor se encarga de guardarlos en los nodos correspondientes y de hacerles su respectiva particion, responde al usuario con un 200

- `Solucion:` Los archivos los maneja el backend

## [ ] 4. Versionamiento Chunks

> [!WARNING]
> Los chunks se estan guardando con una variable "version" inventada ("zz"), revisar si es un requisito el versionamiento o si se puede quitar

- `Solucion:` **Pendiente**

## [ ] 5. Validacion de DTOs

> [!WARNING]
> Actualmente los DTOs no tienen comprobaciones (Si es nulo, si tiene valores validos etc), es importante validarlos. **PRINCIPALMENTE** en FileService

- `Solucion:` **Pendiente**

## [x] ~~6. FileService~~

> [!IMPORTANT]
> Esta clase tiene mucho codigo (puede estar violando Single Responsability). Se puede pensar en implementar el patron `Strategy` 

- `Solucion:` Ya se corrigio, revisar solucion en [14]((#x-14-segregar-fileservice))

## [ ] 7. FileProcessingException

> [!TIP]
> Revisar como hacer que una "exception" reciva 2 parametros (el texto y el error `e`), y para que sirve hacerlo

- `Solucion:` **Pendiente**

## [ ] 8. StorageService

> [!CAUTION]
> CONFIGURAR BIEN EL SERVICIO, los metodos actualmente suben todo al **mismo nodo**

- `Solucion:` **Pendiente**

## [ ] 9. StorageService, `copyAnotherToNode`

> [!CAUTION]
> En el metodo `copyAnotherToNode` hay que revisar que parametros recibir, para poder copiar el nodo

- `Solucion:` **Pendiente**


## [x] ~~10. COREGIR BASE DE DATOS~~

> [!Caution]
> Redefinir la base de datos, principalmente la Entidad nodo para que almacene los registros necesarios acorde a la funcionalidad

- `Solucion:` Ya se actualizo, revisar **nota 20**


## [ ] 11. ValueObjects

> [!TIP]
> Revisar si se pueden limpiar (eliminar) algunos `valueObjects`

- `Solucion:` **Pendiente**


## [x] ~~12. obtencion de los nodos para la replicacion~~

> [!NOTE]
> Se esta haciendo un machetazo (se obtiene el doble de nodos para asegurar que no se repitan), revisar si hay una solucion mas elegante y optima. Se puede crear una cola que vaya revisando iterativamente si el archivo ya se replico a x nodos (toca implementar la funcion )

- `Solucion:` Se creo el metodo `getDifferentsNodesIds`, revisar si hace falta tener el metodo `getRandomNodeAssignments` o se pueden unir


## [ ] 13. Implementar patron SAGA

> [!WARNING]
> Actualmente, a la hora de particionar, si de 5 chunks se envian 4 pero el 5 falla, el problema no guarda la metadata, pero se guardan los archivos en los nodos (Se genera basura). Se puede implementar un patro SAGA pero hay que ver como hacerlo 

- `Solucion:` **Pendiente**

## [x] ~~14. Segregar FileService~~

> [!WARNING]
> El servicio de archivos funciona correctamente, pero esta rompiendo los principios solid **Single Responsability** y **Open/Close**, se puede segregar en las siguientes clases: 

| Componente | Responsabilidad |
| --- | --- |
| `FileService` | Orquestar la creación y particionado del archivo |
| `FilePartitionService` | Calcular y producir los chunks |
| `NodeAssignmentService` | Seleccionar los nodos de almacenamiento |
| `ChunkStorageService` | Almacenar chunks y construir metadata |
| `ChunkReplicationService` | Replicar y verificar chunks |

- `Solucion:` Se segrego correctamente el `FileService`, ahora cumple con **Single Responsability**

## [x] ~~15. Quitar fachadas~~

> [!TIP]
> Actualmente los metodos `ChunkReplicationService` y `ChunkStorageService` tienen metodos fachada, revisar hacen falta

- `Solucion:` Se opto por eliminarlas, no hacia falta tenerlas 

## [ ] 16. Cambiar tamaño chunks

> [!IMPORTANT]
> Al parecer los chunks tiene un tamaño de 3Mb, no de 32Mb como se quiere, revisar como configurarlo bien en `FilePartitionService`

- `Solucion:` **Pendiente**

## [ ] 17. Hacer algo cuando se detecta que el chunk esta corrupto (No coincide el hash)

> [!WARNING]
> Actualmente, cuando un bloque esta corrupto solo lanza la exception, pero no replica ni lo actualiza

- `Solucion:` **Pendiente**

## [ ] 18. Revisar el catch del metodo `mergeFile` en `FileReconstructionService`

> [!TIP]
> Revisar como configurarlo para que use una exceptino y no un IOException 

- `Solucion:` **Pendiente**


## [ ] 19. Revisar Refactorizacion `lookOverIntegrity`

> [!NOTE]
> Revisar en que clase deberia ir el metodo `lookOverIntegrity`, si en `FileService` o en `ChunkStorageService`

- `Solucion:` **Pendiente**


## [x] ~~20. Actualizar la base de datos~~

> [!CAUTION]
> La base de datos esta desactualizada respecto a las entidades del backend, actualiza principalmente `File`, ahora tiene el numero de la parte

- `Solucion:` Ya se actualizo la base de datos respecto a lo implementado en el BackEnd. Se actualizaron las tablas y se crearon los procesos almacenados

## [ ] 21. Revisar Exceptions en las nuevas clases de Seguridad

> [!IMPORTANT]
> La implementacion de las clases de Seguridad estan usando exception generales, no los especificos, se deberia manejar un mejor control de errores para estas clases con exception especializadas y especificas

- `Solucion:` **Pendiente**

## [ ] 22. Implementar Rol Admin

> [!CAUTION]
> Actualmente, solo se tiene el rol de user, y se le da a cualquier usuario *authorized*, se debe implementar un usuario *Admin* para poder gestionoar y monitorear el sistema

- `Solucion:` **Pendiente**

## [ ] 23. Implementar el GlobalHandlerException

> [!IMPORTANT]
> ya esta creado, pero no abarca todas las *exception* del dominio, es mejor mapearlas para dar respuestas http acordes

- `Solucion:` **Pendiente**

## [ ] 24. Revisar si hace falta tener *input-output* y *request-response*

> [!NOTE]
> Se tienen implementados ambos, pero solo se esta agregando una capa extra (se mappean directamente igual, sin cambios)

- `Solucion:` **Pendiente**

Virgulilla: ~