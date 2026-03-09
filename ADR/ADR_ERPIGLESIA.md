# Estructura de nuestro ADR propuesto para Gestion Parroquial
# ADR-001 Refactorizacion del modulo de autentificacion por vulnerabilidades  criticas de seguridad y violaciones a SOLID

## Estado del ADR
### Propuesto/Implementado
Hicimos 10 propuestas de decision, de estas solo sacamos 6 decisiones para implementarlo en el codigo

## Contexto 
### Objetivo del ADR
Estamos diagnosticando en el sistema propuesto llamado ERP_IGLESIAS y vamos a realizar unas mejoras arquitectonicas aplicando patrones creacional y estructurales + principios SOLID donde queremos encontrar un mejor:

1) Mejorar mantenibilidad(Codigo mas claro, menos duplicacion).
2) Mejorar escalibilidad(agregar features sin romper lo existente).
3) Reducir acoplamiento(modulos menos dependientes entre si).
4) Mantener funcionalidad intacta(pruebas funcionales).

### Diagnostico del stack actual
** Backend: Java + Spring (REST, Security, JPA/Hibernate, Validation)**
** DB: PostgreSQL **
** Frontend: Angular + TypeScript **
** Infra: Docker Compose (servicios app + db) **

### Estructura de carpetas 
** Backend con controladores REST, repositorios JPA y configuración de seguridad/JWT **
** Frontend con módulos Angular, servicios de consumo API, rutas y componentes **

### Hallazgos de diagnostico 
** Controllers con responsabilidades mixtas: validaciones, reglas, lógica de negocio y acceso a repositorio en la misma clase → viola SRP (Single Responsibility)**
** Duplicación de lógica de mapeo (request → entity / entity → response) en varios puntos → sube el acoplamiento **
** Modelo de datos con referencias por ID “suelto” en algunos casos (ej: paymentId como número) → dificulta integridad y MER ** 
** Falta de “puntos centrales” para construir respuestas complejas (ej: dashboard), lo que suele crecer en complejidad con el tiempo**

### Decisiones 
Hicimos 10 decisiones de formato recomendado que seria

A) Patron/SOLID  
B) Donde  
C) Problema actual  
D) Decision  
E) Beneficio 

### Decision 1 - Extraer capa service para Auth (SRP)

A) Patrón/SOLID: SRP + separación Controller/Service

B) Dónde: módulo de autenticación (AuthController/Auth endpoints) 

C) Problema: Controller hace validación + negocio + generación de token

D) Decisión: crear AuthService para manejar login/registro y dejar controller solo como orquestador HTTP

E) Mejora: código testeable, menos acoplamiento, mayor claridad

### Decisión 2 — Extraer ChurchService (SRP)
A) Patrón/SOLID: SRP

B) Dónde: ChurchController / reglas de creación/actualización

C) Problema: reglas de negocio mezcladas con capa HTTP

D) Decisión: ChurchService concentra reglas como “solo una iglesia”, validaciones y persistencia

E) Mejora: evita duplicación y reduce el “controller gordo”

### Decisión 3 — Extraer UserService (SRP)
A) Patrón/SOLID: SRP

B) Dónde: UserController

C) Problema: lógica de usuario dispersa en endpoints

D) Decisión: centralizar en UserService (create/update/validaciones)

E) Mejora: mantenibilidad y coherencia

### Decisión 4 — Introducir DTOs para request/response (Adapter)
A) Patrón/SOLID: Adapter + SRP

B) Dónde: endpoints (Auth, Church, User, etc.)

C) Problema: exponer entidades directamente o mezclar formatos

D) Decisión: crear DTOs: LoginRequest, RegisterRequest, UserResponse, etc.

E) Mejora: desacopla capa API del modelo de persistencia

### Decisión 5 — Mapper/Factory para conversiones (Factory Method / Simple Factory)
A) Patrón/SOLID: Factory Method / Simple Factory

B) Dónde: capa DTO ↔ Entity

C) Problema: mapeos repetidos con new y setters en muchos sitios

D) Decisión: UserMapper, ChurchMapper y/o MapperFactory

E) Mejora: reduces duplicación y estandarizas construcción

### Decisión 6 — GlobalExceptionHandler (estandarizar errores)
A) Patrón/SOLID: estructural (centralización de manejo de errores) + SRP

B) Dónde: @ControllerAdvice

C) Problema: errores inconsistentes y try/catch repetidos

D) Decisión: handler global para NotFound, BadRequest, Unauthorized, etc.

E) Mejora: respuestas uniformes, mejor UX, menos repetición

### Decisión 7 — Facade para flujos complejos (Facade)
A) Patrón/SOLID: Facade

B) Dónde: casos tipo “inscripción” o “dashboard”

C) Problema: endpoints que con el tiempo terminan juntando muchos repos/servicios

D) Decisión: EnrollmentFacade o DashboardFacade para coordinar pasos

E) Mejora: reduce acoplamiento y simplifica controllers

### Decisión 8 — Strategy para lógica variable de pagos (Strategy)
A) Patrón/SOLID: Strategy + OCP

B) Dónde: procesamiento de pagos o estados

C) Problema: condicionales por tipo (crece con el tiempo)

D) Decisión: PaymentStrategy + implementaciones por tipo/medio

E) Mejora: agregas tipos sin modificar la lógica central (OCP)

### Decisión 9 — Formalizar relación Payment en el modelo (diseño de datos)
A) Patrón/SOLID: diseño de dominio / integridad

B) Dónde: entidades Enrollment/Offering si tienen paymentId suelto

C) Problema: referencias por ID sin FK real

D) Decisión: relación JPA (@ManyToOne Payment payment)

E) Mejora: integridad, queries más claras, MER real

### Decisión 10 — Separar configuración de seguridad/JWT (SRP)
A) Patrón/SOLID: SRP

B) Dónde: SecurityConfig, JwtService, filtros

C) Problema: auth mezclada con otras responsabilidades

D) Decisión: centralizar config de seguridad y servicios JWT

E) Mejora: cambios de seguridad no rompen dominios

## Cambios implementados 
### implementado 1 - AuthService SRP
1) Ubicación: backend/src/.../AuthController.java, AuthService.java

2) Antes: controller contenía lógica de autenticación y token

3) Después: controller delega a AuthService y retorna respuesta

4) Prueba funcional: Login válido → acceso normal; login inválido → error controlado (captura/video)

### implementado 2 - ChurchService SRP 
1) Ubicación: backend/src/.../churchController.java, churchService.java

2) Antes: controller contenía lógica de negocio, validaciones y acceso a repositorio.

3) Después: Se creó ChurchService para encapsular la lógica de creación y consulta de iglesia, dejando al controller únicamente como punto de entrada HTTP.

4) Prueba funcional: 200 OK para crear una nueva iglesia, 403 porque no deja crear mas de una sola iglesia

### implementado  - UserService SRP 
1) Ubicación: backend/src/.../UserController.java, churchService.java

2) Antes: El UserController contenía lógica de negocio, validaciones de email, encriptación de contraseña y acceso directo al repositorio AppUserRepository, mezclando responsabilidades de la capa HTTP con reglas del dominio.

3) Despues: Se creó UserService para encapsular la lógica de creación de usuarios, validaciones de duplicidad de email y encriptación de contraseña, dejando al UserController únicamente como punto de entrada HTTP que delega las operaciones al servicio.

4) Prueba funcional: 200 OK al crear un nuevo usuario correctamente mediante POST /api/users.403 Forbidden cuando se intenta acceder al endpoint sin un token con rol ADMIN.




