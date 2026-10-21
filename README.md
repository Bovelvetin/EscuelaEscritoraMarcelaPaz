# EscuelaEscritoraMarcelaPaz
Proyecto DUOC UC Sede Plaza Oeste, asignatura Desarrollo de Aplicaciones Móviles.
Desarrollado con Kotlin en Android Studio API 28 "Pie".

## Contexto de la organización.
La Escuela Escritora Marcela Paz es un establecimiento educacional público, gratuito e inclusivo, ubicado en la
comuna de San Bernardo, que atiende aproximadamente a 270 estudiantes desde Pre-Kínder hasta 8.º año de
Enseñanza Básica. Cuenta con salas de clases, oficinas, biblioteca, laboratorios, sala sensorial, CIEMP, patios,
baños, huerto y otros espacios de uso diario, además de mobiliario escolar y equipamiento tecnológico
(computadores, notebooks, proyectores, impresoras, parlantes, equipos de red y recursos de robótica).

### Situación actual. 
Actualmente, los problemas de infraestructura, mobiliario o tecnología se informan de manera verbal, el
funcionario que detecta una dificultad debe comunicarla personalmente al encargado de Tecnología de la
Información, al personal auxiliar o a otro funcionario disponible. Este proceso no genera un registro
centralizado que permita saber cuándo fue informado el problema, quién lo reportó, dónde se encuentra, cuál
es su prioridad o quién quedó responsable de resolverlo, y tampoco permite al solicitante consultar si su
requerimiento fue recibido, asignado, atendido o finalizado.

### Necesidad o problema.
La escuela no cuenta con un sistema digital centralizado para registrar y hacer seguimiento a las solicitudes de
mantención. Al informarse verbalmente, algunos problemas se olvidan, se reportan varias veces o quedan sin
un responsable claro; tampoco resulta fácil establecer un orden de atención ni distinguir lo urgente de lo
secundario. La falta de fotografías, descripciones y ubicaciones registradas dificulta comprender el problema
desde el inicio, y no existe trazabilidad de las acciones realizadas, los comentarios ingresados ni los tiempos de resolución. Esta situación puede generar demoras, confusiones y dificultades para mantener operativos y
seguros los espacios, el mobiliario y el equipamiento tecnológico.

### Mejora esperada.
Se espera centralizar los reportes de mantención de infraestructura, mobiliario y equipamiento tecnológico
mediante una aplicación móvil (MPAZ Ticket) que permita a los funcionarios registrar un problema -categoría,
ubicación, descripción y fotografía opcional- y obtener un número de ticket con un estado claro
(pendiente,revisado, asignado, en proceso, solucionado o cerrado). El encargado de Tecnología de la
Información debería contar con una bandeja central para priorizar, asignar y comentar los tickets; el personal
auxiliar o de mantenimiento debería poder consultar y actualizar los que le sean asignados; y cada solicitante
debería poder revisar el avance de su solicitud sin necesidad de consultar verbalmente.

## Información necesaria para desarrollar la propuesta.
### Tareas o funciones de los perfiles usuarios.
Perfil solicitante (funcionario de la escuela): hoy informa los problemas verbalmente y no recibe comprobante
ni puede verificar su estado. Con la aplicación debería poder crear un ticket, seleccionar la categoría, indicar la
ubicación, ingresar título y descripción, adjuntar fotografías, enviar el reporte, consultar sus solicitudes
anteriores y revisar el estado y los comentarios de cada una. Perfil administrador (encargado de Tecnología de
la Información): hoy recibe avisos verbales y debe recordarlos o anotarlos manualmente. Con la aplicación
debería poder consultar todos los tickets, revisar su descripción, ubicación y fotografías, confirmar la categoría,
definir la prioridad, asignar el ticket al encargado de TI o al personal auxiliar, cambiar su estado, ingresar
comentarios, reasignar cuando sea necesario, confirmar la solución, cerrar el ticket y filtrar el historial por
categoría, prioridad, estado, ubicación, responsable o fecha. Perfil responsable de la solución (encargado de TI
o personal auxiliar o de mantenimiento): debería poder consultar únicamente los tickets asignados, revisar el
problema, la ubicación y las fotografías, cambiar el estado a en proceso, registrar comentarios sobre las
acciones realizadas, indicar si necesita materiales o información adicional, adjuntar evidencia de la reparación
y marcar el ticket como solucionado para su revisión y cierre. Los estudiantes no crean tickets ni tienen cuentas
de acceso; si detectan un problema, deben informarlo a un funcionario.

### Información necesaria para el proceso.
Número o código único del ticket, título y descripción del problema, categoría (infraestructura, tecnología,
mobiliario u otra) y ubicación dentro de la escuela. Fotografías del problema cuando sean necesarias,
identificación y cargo del funcionario solicitante, fecha y hora de creación, y prioridad definida por el encargado
de TI. Estado del ticket (pendiente, revisado, asignado, en proceso, solucionado, cerrado o cancelado), usuario
administrador y funcionario asignado, fechas de asignación, comentarios de solicitante, administrador y
responsable, descripción de las acciones realizadas, materiales o recursos requeridos, evidencia de la solución,
fechas de solución y cierre, e historial de cambios de estado. Para el desarrollo académico se utilizarán
funcionarios, ubicaciones, fotografías, problemas, comentarios y soluciones ficticias o debidamente
anonimizadas.

### Condiciones de uso de la aplicación móvil.
La aplicación se utilizará dentro de la escuela mediante teléfonos móviles, preferentemente con sistema
operativo Android y conexión a internet mediante la red institucional o datos móviles. Podrá usar la cámara
del teléfono para adjuntar fotografías del problema y de su solución, de forma opcional, evitando capturar
rostros, documentos o datos personales identificables. No se utilizará ubicación GPS: el usuario deberá seleccionar o escribir manualmente el espacio donde se encuentra el problema. Debe presentar textos legibles,
botones claros, mensajes de confirmación y una navegación sencilla para usuarios con distintos niveles de
experiencia tecnológica. No se contemplan compras, pagos, presupuestos ni contrataciones mediante la
aplicación.

### Conexión con otros sistemas o herramientas.
Actualmente no existe un sistema institucional de tickets con el cual la aplicación deba integrarse
obligatoriamente; los problemas se informan de manera verbal. El MVP podrá funcionar con una base de datos
de prueba independiente que almacene usuarios, categorías, ubicaciones, tickets, prioridades, asignaciones,
comentarios, estados y fechas. Una futura implementación podría evaluar el envío de notificaciones internas
o avisos al correo institucional cuando un ticket sea creado, asignado, actualizado, solucionado o cerrado, sin
que ello implique compartir cuentas, contraseñas ni credenciales institucionales durante el desarrollo
académico. No se requiere conexión con bases de datos de estudiantes ni con otros sistemas internos de la
escuela, y no se entregarán credenciales, configuraciones de red ni información sensible sobre la
infraestructura tecnológica del establecimiento.

## Criterios para considerar útil la propuesta.
### Resultados o señales de éxito.
Registro centralizado de problemas y solicitudes de mantención, con creación automática de un número
identificador para cada ticket y clasificación en infraestructura, tecnología y mobiliario. Registro de la ubicación
del problema y posibilidad de adjuntar fotografías como evidencia. Definición de prioridad y asignación del
ticket al encargado de TI o al personal auxiliar, con estado claramente identificado y registro de comentarios y
acciones realizadas.
Disminución de problemas olvidados o reportados repetidamente y mayor rapidez para organizar y priorizar
las solicitudes. Consulta del historial de cambios de cada ticket, con fechas de creación, asignación, solución y
cierre, y mayor trazabilidad desde el reporte inicial hasta la finalización del requerimiento.

## Restricciones que se deben considerar.
### Condiciones o límites del caso.
El entregable es un Producto Mínimo Viable (MVP) y no el desarrollo definitivo ni la publicación productiva de
una aplicación. No se contemplan compras, pagos, presupuestos ni contrataciones mediante la aplicación, y
no se utilizará ubicación GPS. El proyecto se desarrollará sin datos personales reales, credenciales ni
información sensible sobre la infraestructura de la escuela: los usuarios, ubicaciones, fotografías, tickets,
comentarios y responsables utilizados en las pruebas deben ser ficticios o estar debidamente anonimizados, y
las fotografías de demostración no deben mostrar rostros de estudiantes o funcionarios ni otros antecedentes
sensibles.
La aplicación no reemplaza los procedimientos institucionales para emergencias; una situación de riesgo
inmediato debe comunicarse por los canales que defina la escuela.



























