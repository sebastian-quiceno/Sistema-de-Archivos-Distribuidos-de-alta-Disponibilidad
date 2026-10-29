# NOTAS A TENER EN CUENTA

## 1. Implementar Seguridad por medio de Spring Security

> [!DANGER]
> Es un requisito del documento, revisar si solo validar autentication o tambien authorization

## 2. Implementar Permisos

> [!MISSING]
> No es critico, ni creo que sea importante, pero se puede implementar

## 3. Envio Archivos

> [!IMPORTANT]
> El archivo lo envia el usuario (FrontEnd) al servidor (Backend), el servidor se encarga de guardarlos en los nodos correspondientes y de hacerles su respectiva particion, responde al usuario con un 200 

## 4. Versionamiento Chunks

> [!WARNING]
> Los chunks se estan guardando con una variable "version" inventada ("zz"), revisar si es un requisito el versionamiento o si se puede quitar