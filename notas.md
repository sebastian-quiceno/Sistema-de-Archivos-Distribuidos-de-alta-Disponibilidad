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

## [ ] 6. FileService

> [!IMPORTANT]
> Esta clase tiene mucho codigo (puede estar violando Single Responsability). Se puede pensar en implementar el patron `Strategy` 

- `Solucion:` **Pendiente**

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


## [ ] 10. COREGIR BASE DE DATOS

> [!Caution]
> Redefinir la base de datos, principalmente la Entidad nodo para que almacene los registros necesarios acorde a la funcionalidad

- `Solucion:` **Pendiente**


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

## [ ] 14. Segregar FileService

> [!WARNING]
> El servicio de archivos funciona correctamente, pero esta rompiendo los principios solid **Single Responsability** y **Open/Close**, se puede segregar en las siguientes clases: 

| Componente | Responsabilidad |
| --- | --- |
| `FileService` | Orquestar la creación y particionado del archivo |
| `FilePartitionService` | Calcular y producir los chunks |
| `NodeAssignmentService` | Seleccionar los nodos de almacenamiento |
| `ChunkStorageService` | Almacenar chunks y construir metadata |
| `ChunkReplicationService` | Replicar y verificar chunks |



