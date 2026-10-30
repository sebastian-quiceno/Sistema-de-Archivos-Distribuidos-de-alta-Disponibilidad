# NOTAS A TENER EN CUENTA

## 1. Implementar Seguridad por medio de Spring Security

> [!CAUTION]
> Es un requisito del documento, revisar si solo validar autentication o tambien authorization

[ ]`Solucion:`

## 2. Implementar Permisos

> [!TIP]
> No es critico, ni creo que sea importante, pero se puede implementar

[ ]`Solucion:`

## ~~3. Envio Archivos~~

> [!IMPORTANT]
> El archivo lo envia el usuario (FrontEnd) al servidor (Backend), el servidor se encarga de guardarlos en los nodos correspondientes y de hacerles su respectiva particion, responde al usuario con un 200

[X]`Solucion:` Los archivos los maneja el backend

## 4. Versionamiento Chunks

> [!WARNING]
> Los chunks se estan guardando con una variable "version" inventada ("zz"), revisar si es un requisito el versionamiento o si se puede quitar

[ ]`Solucion:`

## 5. Validacion de DTOs

> [!WARNING]
> Actualmente los DTOs no tienen comprobaciones (Si es nulo, si tiene valores validos etc), es importante validarlos. **PRINCIPALMENTE** en FileService

[ ]`Solucion:`

## 6. FileService

> [!IMPORTANT]
> Esta clase tiene mucho codigo (puede estar violando Single Responsability). Se puede pensar en implementar el patron `Strategy` 

[ ]`Solucion:`

## 7. FileProcessingException

> [!TIP]
> Revisar como hacer que una "exception" reciva 2 parametros (el texto y el error `e`), y para que sirve hacerlo

[ ]`Solucion:`

## 8. StorageService

> [!CAUTION]
> CONFIGURAR BIEN EL SERVICIO, los metodos actualmente suben todo al **mismo nodo**

[ ]`Solucion:`
