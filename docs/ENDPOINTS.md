# Contrato HTTP de MANÁ

Generado desde el OpenAPI real. Los ejemplos son ilustrativos: use ids existentes y precios de la BD. Los listados paginados contienen `contenido`, `pagina`, `tamanio`, `totalElementos`, `totalPaginas` y `ultima`. `desde`/`hasta` son inclusivos; una página empieza en 0.

<a id="operacion-001"></a>

## GET /api/health

Permiso: **Público**.

Request: sin cuerpo.

Respuesta 200:

```text
Backend funcionando correctamente
```

<a id="operacion-002"></a>

## POST /api/v1/auth/login

Permiso: **Público**.

Request:

```json
{
  "correo": "{{correo}}",
  "clave": "{{clave}}"
}
```

Respuesta 200:

```json
{
  "token": "<JWT>",
  "tipo": "Bearer",
  "expiraEnSegundos": 3600,
  "usuarioId": 1,
  "nombreCompleto": "Dueño MANÁ",
  "rol": "DUENO",
  "localId": 1,
  "localNombre": "Ejemplo",
  "permisos": [
    "USUARIO_GESTIONAR",
    "LOCAL_GESTIONAR",
    "CLIENTE_LEER",
    "CLIENTE_ESCRIBIR",
    "CONTRATO_LEER",
    "CONTRATO_REGISTRAR",
    "CONTRATO_ANULAR",
    "CONSUMO_REGISTRAR",
    "CONSUMO_ANULAR",
    "VENTA_REGISTRAR",
    "VENTA_ANULAR",
    "CAJA_OPERAR",
    "CAJA_SUPERVISAR",
    "MENU_GESTIONAR",
    "MENU_LEER",
    "PLAN_GESTIONAR",
    "CALENDARIO_GESTIONAR",
    "INVENTARIO_GESTIONAR",
    "GASTO_GESTIONAR",
    "REPORTE_VER"
  ]
}
```

<a id="operacion-003"></a>

## GET /api/v1/auth/me

Permiso: **Autenticado**.

Request: sin cuerpo.

Respuesta 200:

```json
{
  "token": "<JWT>",
  "tipo": "Bearer",
  "expiraEnSegundos": 1,
  "usuarioId": 1,
  "nombreCompleto": "Ejemplo",
  "rol": "CAJERO",
  "localId": 1,
  "localNombre": "Ejemplo",
  "permisos": [
    "Ejemplo"
  ]
}
```

<a id="operacion-004"></a>

## POST /api/v1/auth/recuperar

Permiso: **Público**.

Request:

```json
{
  "correo": "{{correo}}"
}
```

Respuesta 200:

```json
{
  "mensaje": "Ejemplo"
}
```

<a id="operacion-005"></a>

## POST /api/v1/auth/restablecer

Permiso: **Público**.

Request:

```json
{
  "token": "{{tokenRestablecimiento}}",
  "nuevaClave": "{{nuevaClave}}"
}
```

Respuesta 200:

```json
{
  "mensaje": "Ejemplo"
}
```

<a id="operacion-006"></a>

## GET /api/v1/bebidas

Permiso: **INVENTARIO_GESTIONAR**.

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "id": 1,
    "nombre": "Ejemplo",
    "precioVenta": 10,
    "activo": true,
    "version": 1
  }
]
```

<a id="operacion-007"></a>

## POST /api/v1/bebidas

Permiso: **INVENTARIO_GESTIONAR**.

Request:

```json
{
  "nombre": "Ejemplo",
  "precioVenta": 10,
  "activo": true
}
```

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "precioVenta": 10,
  "activo": true,
  "version": 1
}
```

<a id="operacion-008"></a>

## GET /api/v1/bebidas/{id}

Permiso: **INVENTARIO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "precioVenta": 10,
  "activo": true,
  "version": 1
}
```

<a id="operacion-009"></a>

## PUT /api/v1/bebidas/{id}

Permiso: **INVENTARIO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "nombre": "Ejemplo",
  "precioVenta": 10,
  "activo": true
}
```

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "precioVenta": 10,
  "activo": true,
  "version": 1
}
```

<a id="operacion-010"></a>

## DELETE /api/v1/bebidas/{id}

Permiso: **INVENTARIO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
null
```

<a id="operacion-011"></a>

## GET /api/v1/cajas

Permiso: **CAJA_SUPERVISAR**.

Parámetros: `desde` (query, opcional), `hasta` (query, opcional), `usuarioId` (query, opcional), `localId` (query, opcional), `pagina` (query, opcional, defecto 0), `tamanio` (query, opcional, defecto 20), `ordenarPor` (query, opcional, defecto id), `direccion` (query, opcional, defecto desc).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "contenido": [
    {
      "id": 1,
      "localId": 1,
      "usuarioId": 1,
      "apertura": "2026-11-03T12:00:00",
      "cierre": "2026-11-03T12:00:00",
      "montoInicial": 10,
      "montoEsperado": 10,
      "montoContado": 10,
      "diferencia": 10,
      "observacion": "Ejemplo",
      "version": 1
    }
  ],
  "pagina": 0,
  "tamanio": 20,
  "totalElementos": 1,
  "totalPaginas": 1,
  "ultima": true
}
```

<a id="operacion-012"></a>

## POST /api/v1/cajas/{id}/cerrar

Permiso: **CAJA_OPERAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "conteos": [
    {
      "metodo": "EFECTIVO",
      "montoContado": 50
    },
    {
      "metodo": "YAPE",
      "montoContado": 0
    }
  ],
  "observacion": "Arqueo de cierre"
}
```

Respuesta 200:

```json
{
  "id": 1,
  "localId": 1,
  "usuarioId": 1,
  "apertura": "2026-11-03T12:00:00",
  "cierre": "2026-11-03T12:00:00",
  "montoInicial": 10,
  "montoEsperado": 10,
  "montoContado": 10,
  "diferencia": 10,
  "observacion": "Ejemplo",
  "version": 1
}
```

<a id="operacion-013"></a>

## GET /api/v1/cajas/{id}/resumen

Permiso: **CAJA_OPERAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "caja": {
    "id": 1,
    "localId": 1,
    "usuarioId": 1,
    "apertura": "2026-11-03T12:00:00",
    "cierre": "2026-11-03T12:00:00",
    "montoInicial": 10,
    "montoEsperado": 10,
    "montoContado": 10,
    "diferencia": 10,
    "observacion": "Ejemplo",
    "version": 1
  },
  "metodos": [
    {
      "metodo": "YAPE",
      "montoEsperado": 10,
      "montoContado": 10,
      "diferencia": 10,
      "movimientos": 1
    }
  ],
  "montoEsperado": 10,
  "ventas": 1,
  "consumos": 1,
  "contratos": 1
}
```

<a id="operacion-014"></a>

## POST /api/v1/cajas/abrir

Permiso: **CAJA_OPERAR**.

Request:

```json
{
  "montoInicial": 50
}
```

Respuesta 200:

```json
{
  "id": 1,
  "localId": 1,
  "usuarioId": 1,
  "apertura": "2026-11-03T12:00:00",
  "cierre": "2026-11-03T12:00:00",
  "montoInicial": 10,
  "montoEsperado": 10,
  "montoContado": 10,
  "diferencia": 10,
  "observacion": "Ejemplo",
  "version": 1
}
```

<a id="operacion-015"></a>

## GET /api/v1/cajas/actual

Permiso: **CAJA_OPERAR**.

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "localId": 1,
  "usuarioId": 1,
  "apertura": "2026-11-03T12:00:00",
  "cierre": "2026-11-03T12:00:00",
  "montoInicial": 10,
  "montoEsperado": 10,
  "montoContado": 10,
  "diferencia": 10,
  "observacion": "Ejemplo",
  "version": 1
}
```

<a id="operacion-016"></a>

## GET /api/v1/catalogos

Permiso: **Autenticado**.

Request: sin cuerpo.

Respuesta 200:

```json
{
  "servicios": [
    {
      "codigo": "Ejemplo",
      "nombre": "Ejemplo",
      "descripcion": "Descripción de ejemplo",
      "activo": true
    }
  ],
  "metodosPago": [
    {
      "codigo": "Ejemplo",
      "nombre": "Ejemplo",
      "descripcion": "Descripción de ejemplo",
      "activo": true
    }
  ],
  "categorias": [
    {
      "codigo": "Ejemplo",
      "nombre": "Ejemplo",
      "descripcion": "Descripción de ejemplo",
      "activo": true
    }
  ],
  "roles": [
    {
      "codigo": "Ejemplo",
      "nombre": "Ejemplo",
      "descripcion": "Descripción de ejemplo",
      "activo": true
    }
  ],
  "diasAtencion": [
    {
      "servicio": "D",
      "diaSemana": 1
    }
  ]
}
```

<a id="operacion-017"></a>

## GET /api/v1/clientes

Permiso: **CLIENTE_LEER**.

Parámetros: `q` (query, opcional), `pagina` (query, opcional, defecto 0), `tamanio` (query, opcional, defecto 20), `ordenarPor` (query, opcional, defecto id), `direccion` (query, opcional, defecto desc).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "contenido": [
    {
      "id": 1,
      "dni": "87654321",
      "nombre": "Ejemplo",
      "apellido": "Pérez",
      "telefono": "999999999",
      "activo": true,
      "version": 1
    }
  ],
  "pagina": 0,
  "tamanio": 20,
  "totalElementos": 1,
  "totalPaginas": 1,
  "ultima": true
}
```

<a id="operacion-018"></a>

## POST /api/v1/clientes

Permiso: **CLIENTE_ESCRIBIR**.

Request:

```json
{
  "dni": "87654321",
  "nombre": "Ejemplo",
  "apellido": "Pérez",
  "telefono": "999999999",
  "activo": true
}
```

Respuesta 200:

```json
{
  "id": 1,
  "dni": "87654321",
  "nombre": "Ejemplo",
  "apellido": "Pérez",
  "telefono": "999999999",
  "activo": true,
  "version": 1
}
```

<a id="operacion-019"></a>

## GET /api/v1/clientes/{id}

Permiso: **CLIENTE_LEER**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "dni": "87654321",
  "nombre": "Ejemplo",
  "apellido": "Pérez",
  "telefono": "999999999",
  "activo": true,
  "version": 1
}
```

<a id="operacion-020"></a>

## PUT /api/v1/clientes/{id}

Permiso: **CLIENTE_ESCRIBIR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "dni": "87654321",
  "nombre": "Ejemplo",
  "apellido": "Pérez",
  "telefono": "999999999",
  "activo": true
}
```

Respuesta 200:

```json
{
  "id": 1,
  "dni": "87654321",
  "nombre": "Ejemplo",
  "apellido": "Pérez",
  "telefono": "999999999",
  "activo": true,
  "version": 1
}
```

<a id="operacion-021"></a>

## DELETE /api/v1/clientes/{id}

Permiso: **CLIENTE_ESCRIBIR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
null
```

<a id="operacion-022"></a>

## GET /api/v1/clientes/{id}/contrato-vigente

Permiso: **CONTRATO_LEER**.

Parámetros: `id` (path, obligatorio), `localId` (query, opcional).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "numero": "Ejemplo",
  "localId": 1,
  "clienteId": 1,
  "dni": "87654321",
  "clienteNombre": "Ejemplo",
  "planId": 1,
  "planNombre": "Ejemplo",
  "estado": "PROGRAMADO",
  "fechaInicio": "2026-11-03",
  "fechaFin": "2026-11-03",
  "montoTotal": 10,
  "anteriorId": 1,
  "anuladoEn": "2026-11-03T12:00:00",
  "motivoAnulacion": "Ejemplo",
  "detalles": [
    {
      "id": 1,
      "servicio": "D",
      "racionesContratadas": 1,
      "saldoRaciones": 1,
      "precioRacionPactado": 10,
      "subtotal": 10
    }
  ],
  "pagos": [
    {
      "id": 1,
      "cajaId": 1,
      "tipo": "COBRO",
      "metodo": "YAPE",
      "monto": 10,
      "fecha": "2026-11-03T12:00:00",
      "referencia": "OP-001"
    }
  ],
  "consumos": [
    {
      "id": 1,
      "contratoId": 1,
      "detalleContratoId": 1,
      "servicio": "D",
      "fecha": "2026-11-03",
      "registradoEn": "2026-11-03T12:00:00",
      "valorRacion": 10,
      "paraLlevar": true,
      "recargoLlevar": 10,
      "metodoRecargo": "Ejemplo",
      "cajaId": 1,
      "anulado": true,
      "anuladoEn": "2026-11-03T12:00:00",
      "motivoAnulacion": "Ejemplo",
      "platos": [
        {
          "platoId": 1,
          "platoNombre": "Ejemplo",
          "categoria": "PAN",
          "cantidad": 1
        }
      ]
    }
  ],
  "version": 1
}
```

<a id="operacion-023"></a>

## GET /api/v1/compras-bebidas

Permiso: **INVENTARIO_GESTIONAR**.

Parámetros: `localId` (query, opcional), `desde` (query, opcional), `hasta` (query, opcional).

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "id": 1,
    "localId": 1,
    "proveedorId": 1,
    "proveedorNombre": "Ejemplo",
    "fecha": "2026-11-03",
    "total": 10,
    "detalles": [
      {
        "id": 1,
        "bebidaId": 1,
        "bebidaNombre": "Ejemplo",
        "presentaciones": 1,
        "unidadesPorPresentacion": 1,
        "unidades": 1,
        "costoTotal": 10
      }
    ]
  }
]
```

<a id="operacion-024"></a>

## POST /api/v1/compras-bebidas

Permiso: **INVENTARIO_GESTIONAR**.

Request:

```json
{
  "proveedorId": 1,
  "fecha": "2026-11-03",
  "detalles": [
    {
      "bebidaId": 1,
      "presentaciones": 2,
      "unidadesPorPresentacion": 6,
      "costoTotal": 18
    }
  ]
}
```

Respuesta 200:

```json
{
  "id": 1,
  "localId": 1,
  "proveedorId": 1,
  "proveedorNombre": "Ejemplo",
  "fecha": "2026-11-03",
  "total": 10,
  "detalles": [
    {
      "id": 1,
      "bebidaId": 1,
      "bebidaNombre": "Ejemplo",
      "presentaciones": 1,
      "unidadesPorPresentacion": 1,
      "unidades": 1,
      "costoTotal": 10
    }
  ]
}
```

<a id="operacion-025"></a>

## GET /api/v1/compras-bebidas/{id}

Permiso: **INVENTARIO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "localId": 1,
  "proveedorId": 1,
  "proveedorNombre": "Ejemplo",
  "fecha": "2026-11-03",
  "total": 10,
  "detalles": [
    {
      "id": 1,
      "bebidaId": 1,
      "bebidaNombre": "Ejemplo",
      "presentaciones": 1,
      "unidadesPorPresentacion": 1,
      "unidades": 1,
      "costoTotal": 10
    }
  ]
}
```

<a id="operacion-026"></a>

## GET /api/v1/consumos

Permiso: **CONTRATO_LEER**.

Parámetros: `contratoId` (query, opcional), `servicio` (query, opcional), `desde` (query, opcional), `hasta` (query, opcional), `localId` (query, opcional), `pagina` (query, opcional, defecto 0), `tamanio` (query, opcional, defecto 20), `ordenarPor` (query, opcional, defecto id), `direccion` (query, opcional, defecto desc).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "contenido": [
    {
      "id": 1,
      "contratoId": 1,
      "detalleContratoId": 1,
      "servicio": "D",
      "fecha": "2026-11-03",
      "registradoEn": "2026-11-03T12:00:00",
      "valorRacion": 10,
      "paraLlevar": true,
      "recargoLlevar": 10,
      "metodoRecargo": "Ejemplo",
      "cajaId": 1,
      "anulado": true,
      "anuladoEn": "2026-11-03T12:00:00",
      "motivoAnulacion": "Ejemplo",
      "platos": [
        {
          "platoId": 1,
          "platoNombre": "Ejemplo",
          "categoria": "PAN",
          "cantidad": 1
        }
      ]
    }
  ],
  "pagina": 0,
  "tamanio": 20,
  "totalElementos": 1,
  "totalPaginas": 1,
  "ultima": true
}
```

<a id="operacion-027"></a>

## POST /api/v1/consumos

Permiso: **CONSUMO_REGISTRAR**.

Request:

```json
{
  "contratoId": 1,
  "servicio": "D",
  "platos": [
    {
      "platoId": 1,
      "cantidad": 2
    },
    {
      "platoId": 2,
      "cantidad": 1
    }
  ],
  "paraLlevar": false
}
```

Respuesta 200:

```json
{
  "id": 1,
  "contratoId": 1,
  "detalleContratoId": 1,
  "servicio": "D",
  "fecha": "2026-11-03",
  "registradoEn": "2026-11-03T12:00:00",
  "valorRacion": 10,
  "paraLlevar": true,
  "recargoLlevar": 10,
  "metodoRecargo": "Ejemplo",
  "cajaId": 1,
  "anulado": true,
  "anuladoEn": "2026-11-03T12:00:00",
  "motivoAnulacion": "Ejemplo",
  "platos": [
    {
      "platoId": 1,
      "platoNombre": "Ejemplo",
      "categoria": "PAN",
      "cantidad": 1
    }
  ]
}
```

<a id="operacion-028"></a>

## PATCH /api/v1/consumos/{id}/anular

Permiso: **CONSUMO_ANULAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "motivo": "Corrección solicitada por el cliente"
}
```

Respuesta 200:

```json
{
  "id": 1,
  "contratoId": 1,
  "detalleContratoId": 1,
  "servicio": "D",
  "fecha": "2026-11-03",
  "registradoEn": "2026-11-03T12:00:00",
  "valorRacion": 10,
  "paraLlevar": true,
  "recargoLlevar": 10,
  "metodoRecargo": "Ejemplo",
  "cajaId": 1,
  "anulado": true,
  "anuladoEn": "2026-11-03T12:00:00",
  "motivoAnulacion": "Ejemplo",
  "platos": [
    {
      "platoId": 1,
      "platoNombre": "Ejemplo",
      "categoria": "PAN",
      "cantidad": 1
    }
  ]
}
```

<a id="operacion-029"></a>

## GET /api/v1/contratos

Permiso: **CONTRATO_LEER**.

Parámetros: `dni` (query, opcional), `estado` (query, opcional), `desde` (query, opcional), `hasta` (query, opcional), `localId` (query, opcional), `pagina` (query, opcional, defecto 0), `tamanio` (query, opcional, defecto 20), `ordenarPor` (query, opcional, defecto id), `direccion` (query, opcional, defecto desc).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "contenido": [
    {
      "id": 1,
      "numero": "Ejemplo",
      "localId": 1,
      "clienteId": 1,
      "dni": "87654321",
      "clienteNombre": "Ejemplo",
      "planId": 1,
      "planNombre": "Ejemplo",
      "estado": "PROGRAMADO",
      "fechaInicio": "2026-11-03",
      "fechaFin": "2026-11-03",
      "montoTotal": 10,
      "anteriorId": 1,
      "anuladoEn": "2026-11-03T12:00:00",
      "motivoAnulacion": "Ejemplo",
      "detalles": [
        {
          "id": 1,
          "servicio": "D",
          "racionesContratadas": 1,
          "saldoRaciones": 1,
          "precioRacionPactado": 10,
          "subtotal": 10
        }
      ],
      "pagos": [
        {
          "id": 1,
          "cajaId": 1,
          "tipo": "COBRO",
          "metodo": "YAPE",
          "monto": 10,
          "fecha": "2026-11-03T12:00:00",
          "referencia": "OP-001"
        }
      ],
      "consumos": [
        {
          "id": 1,
          "contratoId": 1,
          "detalleContratoId": 1,
          "servicio": "D",
          "fecha": "2026-11-03",
          "registradoEn": "2026-11-03T12:00:00",
          "valorRacion": 10,
          "paraLlevar": true,
          "recargoLlevar": 10,
          "metodoRecargo": "Ejemplo",
          "cajaId": 1,
          "anulado": true,
          "anuladoEn": "2026-11-03T12:00:00",
          "motivoAnulacion": "Ejemplo",
          "platos": [
            {
              "platoId": 1,
              "platoNombre": "Ejemplo",
              "categoria": "PAN",
              "cantidad": 1
            }
          ]
        }
      ],
      "version": 1
    }
  ],
  "pagina": 0,
  "tamanio": 20,
  "totalElementos": 1,
  "totalPaginas": 1,
  "ultima": true
}
```

<a id="operacion-030"></a>

## POST /api/v1/contratos

Permiso: **CONTRATO_REGISTRAR**.

Request:

```json
{
  "clienteId": 1,
  "planId": 1,
  "fechaInicio": "2026-11-03",
  "servicios": [
    "D"
  ],
  "pagos": [
    {
      "metodo": "YAPE",
      "monto": 16,
      "referencia": "OP-001"
    }
  ]
}
```

Respuesta 200:

```json
{
  "id": 1,
  "numero": "Ejemplo",
  "localId": 1,
  "clienteId": 1,
  "dni": "87654321",
  "clienteNombre": "Ejemplo",
  "planId": 1,
  "planNombre": "Ejemplo",
  "estado": "PROGRAMADO",
  "fechaInicio": "2026-11-03",
  "fechaFin": "2026-11-03",
  "montoTotal": 10,
  "anteriorId": 1,
  "anuladoEn": "2026-11-03T12:00:00",
  "motivoAnulacion": "Ejemplo",
  "detalles": [
    {
      "id": 1,
      "servicio": "D",
      "racionesContratadas": 1,
      "saldoRaciones": 1,
      "precioRacionPactado": 10,
      "subtotal": 10
    }
  ],
  "pagos": [
    {
      "id": 1,
      "cajaId": 1,
      "tipo": "COBRO",
      "metodo": "YAPE",
      "monto": 10,
      "fecha": "2026-11-03T12:00:00",
      "referencia": "OP-001"
    }
  ],
  "consumos": [
    {
      "id": 1,
      "contratoId": 1,
      "detalleContratoId": 1,
      "servicio": "D",
      "fecha": "2026-11-03",
      "registradoEn": "2026-11-03T12:00:00",
      "valorRacion": 10,
      "paraLlevar": true,
      "recargoLlevar": 10,
      "metodoRecargo": "Ejemplo",
      "cajaId": 1,
      "anulado": true,
      "anuladoEn": "2026-11-03T12:00:00",
      "motivoAnulacion": "Ejemplo",
      "platos": [
        {
          "platoId": 1,
          "platoNombre": "Ejemplo",
          "categoria": "PAN",
          "cantidad": 1
        }
      ]
    }
  ],
  "version": 1
}
```

<a id="operacion-031"></a>

## GET /api/v1/contratos/{id}

Permiso: **CONTRATO_LEER**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "numero": "Ejemplo",
  "localId": 1,
  "clienteId": 1,
  "dni": "87654321",
  "clienteNombre": "Ejemplo",
  "planId": 1,
  "planNombre": "Ejemplo",
  "estado": "PROGRAMADO",
  "fechaInicio": "2026-11-03",
  "fechaFin": "2026-11-03",
  "montoTotal": 10,
  "anteriorId": 1,
  "anuladoEn": "2026-11-03T12:00:00",
  "motivoAnulacion": "Ejemplo",
  "detalles": [
    {
      "id": 1,
      "servicio": "D",
      "racionesContratadas": 1,
      "saldoRaciones": 1,
      "precioRacionPactado": 10,
      "subtotal": 10
    }
  ],
  "pagos": [
    {
      "id": 1,
      "cajaId": 1,
      "tipo": "COBRO",
      "metodo": "YAPE",
      "monto": 10,
      "fecha": "2026-11-03T12:00:00",
      "referencia": "OP-001"
    }
  ],
  "consumos": [
    {
      "id": 1,
      "contratoId": 1,
      "detalleContratoId": 1,
      "servicio": "D",
      "fecha": "2026-11-03",
      "registradoEn": "2026-11-03T12:00:00",
      "valorRacion": 10,
      "paraLlevar": true,
      "recargoLlevar": 10,
      "metodoRecargo": "Ejemplo",
      "cajaId": 1,
      "anulado": true,
      "anuladoEn": "2026-11-03T12:00:00",
      "motivoAnulacion": "Ejemplo",
      "platos": [
        {
          "platoId": 1,
          "platoNombre": "Ejemplo",
          "categoria": "PAN",
          "cantidad": 1
        }
      ]
    }
  ],
  "version": 1
}
```

<a id="operacion-032"></a>

## PATCH /api/v1/contratos/{id}/anular

Permiso: **CONTRATO_ANULAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "motivo": "Corrección solicitada por el cliente"
}
```

Respuesta 200:

```json
{
  "id": 1,
  "numero": "Ejemplo",
  "localId": 1,
  "clienteId": 1,
  "dni": "87654321",
  "clienteNombre": "Ejemplo",
  "planId": 1,
  "planNombre": "Ejemplo",
  "estado": "PROGRAMADO",
  "fechaInicio": "2026-11-03",
  "fechaFin": "2026-11-03",
  "montoTotal": 10,
  "anteriorId": 1,
  "anuladoEn": "2026-11-03T12:00:00",
  "motivoAnulacion": "Ejemplo",
  "detalles": [
    {
      "id": 1,
      "servicio": "D",
      "racionesContratadas": 1,
      "saldoRaciones": 1,
      "precioRacionPactado": 10,
      "subtotal": 10
    }
  ],
  "pagos": [
    {
      "id": 1,
      "cajaId": 1,
      "tipo": "COBRO",
      "metodo": "YAPE",
      "monto": 10,
      "fecha": "2026-11-03T12:00:00",
      "referencia": "OP-001"
    }
  ],
  "consumos": [
    {
      "id": 1,
      "contratoId": 1,
      "detalleContratoId": 1,
      "servicio": "D",
      "fecha": "2026-11-03",
      "registradoEn": "2026-11-03T12:00:00",
      "valorRacion": 10,
      "paraLlevar": true,
      "recargoLlevar": 10,
      "metodoRecargo": "Ejemplo",
      "cajaId": 1,
      "anulado": true,
      "anuladoEn": "2026-11-03T12:00:00",
      "motivoAnulacion": "Ejemplo",
      "platos": [
        {
          "platoId": 1,
          "platoNombre": "Ejemplo",
          "categoria": "PAN",
          "cantidad": 1
        }
      ]
    }
  ],
  "version": 1
}
```

<a id="operacion-033"></a>

## POST /api/v1/contratos/{id}/renovar

Permiso: **CONTRATO_REGISTRAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "planId": 1,
  "servicios": [
    "D"
  ],
  "pagos": [
    {
      "metodo": "YAPE",
      "monto": 16
    }
  ]
}
```

Respuesta 200:

```json
{
  "id": 1,
  "numero": "Ejemplo",
  "localId": 1,
  "clienteId": 1,
  "dni": "87654321",
  "clienteNombre": "Ejemplo",
  "planId": 1,
  "planNombre": "Ejemplo",
  "estado": "PROGRAMADO",
  "fechaInicio": "2026-11-03",
  "fechaFin": "2026-11-03",
  "montoTotal": 10,
  "anteriorId": 1,
  "anuladoEn": "2026-11-03T12:00:00",
  "motivoAnulacion": "Ejemplo",
  "detalles": [
    {
      "id": 1,
      "servicio": "D",
      "racionesContratadas": 1,
      "saldoRaciones": 1,
      "precioRacionPactado": 10,
      "subtotal": 10
    }
  ],
  "pagos": [
    {
      "id": 1,
      "cajaId": 1,
      "tipo": "COBRO",
      "metodo": "YAPE",
      "monto": 10,
      "fecha": "2026-11-03T12:00:00",
      "referencia": "OP-001"
    }
  ],
  "consumos": [
    {
      "id": 1,
      "contratoId": 1,
      "detalleContratoId": 1,
      "servicio": "D",
      "fecha": "2026-11-03",
      "registradoEn": "2026-11-03T12:00:00",
      "valorRacion": 10,
      "paraLlevar": true,
      "recargoLlevar": 10,
      "metodoRecargo": "Ejemplo",
      "cajaId": 1,
      "anulado": true,
      "anuladoEn": "2026-11-03T12:00:00",
      "motivoAnulacion": "Ejemplo",
      "platos": [
        {
          "platoId": 1,
          "platoNombre": "Ejemplo",
          "categoria": "PAN",
          "cantidad": 1
        }
      ]
    }
  ],
  "version": 1
}
```

<a id="operacion-034"></a>

## POST /api/v1/contratos/simular

Permiso: **CONTRATO_REGISTRAR**.

Request:

```json
{
  "clienteId": 1,
  "planId": 1,
  "fechaInicio": "2026-11-03",
  "servicios": [
    "D"
  ]
}
```

Respuesta 200:

```json
{
  "clienteId": 1,
  "planId": 1,
  "fechaInicio": "2026-11-03",
  "fechaFin": "2026-11-03",
  "servicios": [
    {
      "servicio": "D",
      "raciones": 1,
      "precioRacion": 10,
      "subtotal": 10
    }
  ],
  "total": 10
}
```

<a id="operacion-035"></a>

## GET /api/v1/dias-no-atencion

Permiso: **CALENDARIO_GESTIONAR**.

Parámetros: `localId` (query, opcional), `desde` (query, opcional), `hasta` (query, opcional).

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "id": 1,
    "localId": 1,
    "fecha": "2026-11-03",
    "motivo": "Corrección solicitada por el cliente",
    "version": 1
  }
]
```

<a id="operacion-036"></a>

## POST /api/v1/dias-no-atencion

Permiso: **CALENDARIO_GESTIONAR**.

Request:

```json
{
  "fecha": "2026-11-03",
  "motivo": "Corrección solicitada por el cliente"
}
```

Respuesta 200:

```json
{
  "id": 1,
  "localId": 1,
  "fecha": "2026-11-03",
  "motivo": "Corrección solicitada por el cliente",
  "version": 1
}
```

<a id="operacion-037"></a>

## PUT /api/v1/dias-no-atencion/{id}

Permiso: **CALENDARIO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "fecha": "2026-11-03",
  "motivo": "Corrección solicitada por el cliente"
}
```

Respuesta 200:

```json
{
  "id": 1,
  "localId": 1,
  "fecha": "2026-11-03",
  "motivo": "Corrección solicitada por el cliente",
  "version": 1
}
```

<a id="operacion-038"></a>

## DELETE /api/v1/dias-no-atencion/{id}

Permiso: **CALENDARIO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
null
```

<a id="operacion-039"></a>

## GET /api/v1/gastos

Permiso: **GASTO_GESTIONAR**.

Parámetros: `localId` (query, opcional), `desde` (query, opcional), `hasta` (query, opcional), `pagina` (query, opcional, defecto 0), `tamanio` (query, opcional, defecto 20), `ordenarPor` (query, opcional, defecto id), `direccion` (query, opcional, defecto desc).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "contenido": [
    {
      "id": 1,
      "localId": 1,
      "fecha": "2026-11-03",
      "monto": 10,
      "descripcion": "Descripción de ejemplo",
      "activo": true,
      "version": 1
    }
  ],
  "pagina": 0,
  "tamanio": 20,
  "totalElementos": 1,
  "totalPaginas": 1,
  "ultima": true
}
```

<a id="operacion-040"></a>

## POST /api/v1/gastos

Permiso: **GASTO_GESTIONAR**.

Request:

```json
{
  "fecha": "2026-11-03",
  "monto": 10,
  "descripcion": "Descripción de ejemplo",
  "activo": true
}
```

Respuesta 200:

```json
{
  "id": 1,
  "localId": 1,
  "fecha": "2026-11-03",
  "monto": 10,
  "descripcion": "Descripción de ejemplo",
  "activo": true,
  "version": 1
}
```

<a id="operacion-041"></a>

## GET /api/v1/gastos/{id}

Permiso: **GASTO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "localId": 1,
  "fecha": "2026-11-03",
  "monto": 10,
  "descripcion": "Descripción de ejemplo",
  "activo": true,
  "version": 1
}
```

<a id="operacion-042"></a>

## PUT /api/v1/gastos/{id}

Permiso: **GASTO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "fecha": "2026-11-03",
  "monto": 10,
  "descripcion": "Descripción de ejemplo",
  "activo": true
}
```

Respuesta 200:

```json
{
  "id": 1,
  "localId": 1,
  "fecha": "2026-11-03",
  "monto": 10,
  "descripcion": "Descripción de ejemplo",
  "activo": true,
  "version": 1
}
```

<a id="operacion-043"></a>

## DELETE /api/v1/gastos/{id}

Permiso: **GASTO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
null
```

<a id="operacion-044"></a>

## GET /api/v1/locales

Permiso: **LOCAL_GESTIONAR**.

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "id": 1,
    "nombre": "Ejemplo",
    "direccion": "Dirección del local",
    "telefono": "999999999",
    "activo": true,
    "version": 1
  }
]
```

<a id="operacion-045"></a>

## POST /api/v1/locales

Permiso: **LOCAL_GESTIONAR**.

Request:

```json
{
  "nombre": "Ejemplo",
  "direccion": "Dirección del local",
  "telefono": "999999999",
  "activo": true
}
```

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "direccion": "Dirección del local",
  "telefono": "999999999",
  "activo": true,
  "version": 1
}
```

<a id="operacion-046"></a>

## GET /api/v1/locales/{id}

Permiso: **LOCAL_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "direccion": "Dirección del local",
  "telefono": "999999999",
  "activo": true,
  "version": 1
}
```

<a id="operacion-047"></a>

## PUT /api/v1/locales/{id}

Permiso: **LOCAL_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "nombre": "Ejemplo",
  "direccion": "Dirección del local",
  "telefono": "999999999",
  "activo": true
}
```

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "direccion": "Dirección del local",
  "telefono": "999999999",
  "activo": true,
  "version": 1
}
```

<a id="operacion-048"></a>

## DELETE /api/v1/locales/{id}

Permiso: **LOCAL_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
null
```

<a id="operacion-049"></a>

## GET /api/v1/menus

Permiso: **MENU_LEER**.

Parámetros: `localId` (query, opcional), `desde` (query, opcional), `hasta` (query, opcional), `servicio` (query, opcional).

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "id": 1,
    "localId": 1,
    "fecha": "2026-11-03",
    "servicio": "D",
    "platos": [
      {
        "platoId": 1,
        "nombre": "Ejemplo",
        "categoria": "PAN"
      }
    ],
    "version": 1
  }
]
```

<a id="operacion-050"></a>

## PUT /api/v1/menus

Permiso: **MENU_GESTIONAR**.

Request:

```json
{
  "fecha": "2026-11-03",
  "servicio": "D",
  "platoIds": [
    1,
    2
  ]
}
```

Respuesta 200:

```json
{
  "id": 1,
  "localId": 1,
  "fecha": "2026-11-03",
  "servicio": "D",
  "platos": [
    {
      "platoId": 1,
      "nombre": "Ejemplo",
      "categoria": "PAN"
    }
  ],
  "version": 1
}
```

<a id="operacion-051"></a>

## POST /api/v1/menus/clonar

Permiso: **MENU_GESTIONAR**.

Request:

```json
{
  "fechaOrigen": "2026-11-03",
  "fechaDestino": "2026-11-04",
  "servicios": [
    "D"
  ]
}
```

Respuesta 200:

```json
[
  {
    "id": 1,
    "localId": 1,
    "fecha": "2026-11-03",
    "servicio": "D",
    "platos": [
      {
        "platoId": 1,
        "nombre": "Ejemplo",
        "categoria": "PAN"
      }
    ],
    "version": 1
  }
]
```

<a id="operacion-052"></a>

## POST /api/v1/menus/copiar-a-cena

Permiso: **MENU_GESTIONAR**.

Request:

```json
{
  "fecha": "2026-11-03"
}
```

Respuesta 200:

```json
{
  "id": 1,
  "localId": 1,
  "fecha": "2026-11-03",
  "servicio": "D",
  "platos": [
    {
      "platoId": 1,
      "nombre": "Ejemplo",
      "categoria": "PAN"
    }
  ],
  "version": 1
}
```

<a id="operacion-053"></a>

## GET /api/v1/movimientos-stock

Permiso: **INVENTARIO_GESTIONAR**.

Parámetros: `localId` (query, opcional), `bebidaId` (query, opcional), `desde` (query, opcional), `hasta` (query, opcional), `pagina` (query, opcional, defecto 0), `tamanio` (query, opcional, defecto 20), `ordenarPor` (query, opcional, defecto id), `direccion` (query, opcional, defecto desc).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "contenido": [
    {
      "id": 1,
      "localId": 1,
      "bebidaId": 1,
      "bebidaNombre": "Ejemplo",
      "tipo": "COMPRA",
      "cantidad": 1,
      "fecha": "2026-11-03T12:00:00",
      "detalleCompraId": 1,
      "detalleOrdenId": 1,
      "motivo": "Corrección solicitada por el cliente"
    }
  ],
  "pagina": 0,
  "tamanio": 20,
  "totalElementos": 1,
  "totalPaginas": 1,
  "ultima": true
}
```

<a id="operacion-054"></a>

## GET /api/v1/pensionistas/buscar

Permiso: **CONTRATO_LEER**.

Parámetros: `q` (query, obligatorio), `localId` (query, opcional).

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "clienteId": 1,
    "dni": "87654321",
    "nombreCompleto": "Ejemplo",
    "contratoId": 1,
    "numero": "Ejemplo",
    "estado": "PROGRAMADO",
    "fechaInicio": "2026-11-03",
    "fechaFin": "2026-11-03",
    "servicios": [
      {
        "id": 1,
        "servicio": "D",
        "racionesContratadas": 1,
        "saldoRaciones": 1,
        "precioRacionPactado": 10,
        "subtotal": 10
      }
    ]
  }
]
```

<a id="operacion-055"></a>

## GET /api/v1/planes

Permiso: **PLAN_GESTIONAR**.

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "id": 1,
    "nombre": "Ejemplo",
    "periodo": "SEMANAL",
    "activo": true,
    "servicios": [
      "Ejemplo"
    ],
    "version": 1
  }
]
```

<a id="operacion-056"></a>

## POST /api/v1/planes

Permiso: **PLAN_GESTIONAR**.

Request:

```json
{
  "nombre": "Semanal",
  "periodo": "SEMANAL",
  "activo": true,
  "servicios": [
    "D",
    "A",
    "C"
  ]
}
```

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "periodo": "SEMANAL",
  "activo": true,
  "servicios": [
    "Ejemplo"
  ],
  "version": 1
}
```

<a id="operacion-057"></a>

## GET /api/v1/planes/{id}

Permiso: **PLAN_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "periodo": "SEMANAL",
  "activo": true,
  "servicios": [
    "Ejemplo"
  ],
  "version": 1
}
```

<a id="operacion-058"></a>

## PUT /api/v1/planes/{id}

Permiso: **PLAN_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "nombre": "Semanal",
  "periodo": "SEMANAL",
  "activo": true,
  "servicios": [
    "D",
    "A",
    "C"
  ]
}
```

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "periodo": "SEMANAL",
  "activo": true,
  "servicios": [
    "Ejemplo"
  ],
  "version": 1
}
```

<a id="operacion-059"></a>

## DELETE /api/v1/planes/{id}

Permiso: **PLAN_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
null
```

<a id="operacion-060"></a>

## GET /api/v1/platos

Permiso: **MENU_LEER**.

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "id": 1,
    "nombre": "Ejemplo",
    "categoria": "PAN",
    "activo": true,
    "version": 1
  }
]
```

<a id="operacion-061"></a>

## POST /api/v1/platos

Permiso: **MENU_GESTIONAR**.

Request:

```json
{
  "nombre": "Ejemplo",
  "categoria": "PAN",
  "activo": true
}
```

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "categoria": "PAN",
  "activo": true,
  "version": 1
}
```

<a id="operacion-062"></a>

## GET /api/v1/platos/{id}

Permiso: **MENU_LEER**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "categoria": "PAN",
  "activo": true,
  "version": 1
}
```

<a id="operacion-063"></a>

## PUT /api/v1/platos/{id}

Permiso: **MENU_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "nombre": "Ejemplo",
  "categoria": "PAN",
  "activo": true
}
```

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "categoria": "PAN",
  "activo": true,
  "version": 1
}
```

<a id="operacion-064"></a>

## DELETE /api/v1/platos/{id}

Permiso: **MENU_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
null
```

<a id="operacion-065"></a>

## GET /api/v1/pos/catalogo

Permiso: **VENTA_REGISTRAR**.

Parámetros: `servicio` (query, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "localId": 1,
  "fecha": "2026-11-03",
  "servicio": "D",
  "menu": {
    "id": 1,
    "localId": 1,
    "fecha": "2026-11-03",
    "servicio": "D",
    "platos": [
      {
        "platoId": 1,
        "nombre": "Ejemplo",
        "categoria": "PAN"
      }
    ],
    "version": 1
  },
  "reglas": [
    {
      "id": 1,
      "nombre": "Ejemplo",
      "servicio": "D",
      "precioCombo": 10,
      "recargoLlevar": 10,
      "activo": true,
      "componentes": [
        {
          "categoria": "PAN",
          "cantidad": 1
        }
      ],
      "version": 1
    }
  ],
  "sueltos": [
    {
      "platoId": 1,
      "nombre": "Ejemplo",
      "categoria": "PAN",
      "precio": 10,
      "recargoLlevar": 10
    }
  ],
  "bebidas": [
    {
      "bebidaId": 1,
      "nombre": "Ejemplo",
      "precioVenta": 10,
      "stock": 1
    }
  ]
}
```

<a id="operacion-066"></a>

## GET /api/v1/precios-sueltos

Permiso: **MENU_LEER**.

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "servicio": "D",
    "categoria": "PAN",
    "precio": 10,
    "recargoLlevar": 10,
    "activo": true
  }
]
```

<a id="operacion-067"></a>

## POST /api/v1/precios-sueltos

Permiso: **MENU_GESTIONAR**.

Request:

```json
{
  "servicio": "D",
  "categoria": "PAN",
  "precio": 1.5,
  "recargoLlevar": 0,
  "activo": true
}
```

Respuesta 200:

```json
{
  "servicio": "D",
  "categoria": "PAN",
  "precio": 10,
  "recargoLlevar": 10,
  "activo": true
}
```

<a id="operacion-068"></a>

## PUT /api/v1/precios-sueltos/{servicio}/{categoria}

Permiso: **MENU_GESTIONAR**.

Parámetros: `servicio` (path, obligatorio), `categoria` (path, obligatorio).

Request:

```json
{
  "servicio": "D",
  "categoria": "PAN",
  "precio": 10,
  "recargoLlevar": 10,
  "activo": true
}
```

Respuesta 200:

```json
{
  "servicio": "D",
  "categoria": "PAN",
  "precio": 10,
  "recargoLlevar": 10,
  "activo": true
}
```

<a id="operacion-069"></a>

## DELETE /api/v1/precios-sueltos/{servicio}/{categoria}

Permiso: **MENU_GESTIONAR**.

Parámetros: `servicio` (path, obligatorio), `categoria` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
null
```

<a id="operacion-070"></a>

## GET /api/v1/proveedores

Permiso: **INVENTARIO_GESTIONAR**.

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "id": 1,
    "razonSocial": "Proveedor de ejemplo",
    "telefono": "999999999",
    "activo": true,
    "bebidaIds": [
      1
    ],
    "version": 1
  }
]
```

<a id="operacion-071"></a>

## POST /api/v1/proveedores

Permiso: **INVENTARIO_GESTIONAR**.

Request:

```json
{
  "razonSocial": "Proveedor de ejemplo",
  "telefono": "999999999",
  "activo": true,
  "bebidaIds": [
    1
  ]
}
```

Respuesta 200:

```json
{
  "id": 1,
  "razonSocial": "Proveedor de ejemplo",
  "telefono": "999999999",
  "activo": true,
  "bebidaIds": [
    1
  ],
  "version": 1
}
```

<a id="operacion-072"></a>

## GET /api/v1/proveedores/{id}

Permiso: **INVENTARIO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "razonSocial": "Proveedor de ejemplo",
  "telefono": "999999999",
  "activo": true,
  "bebidaIds": [
    1
  ],
  "version": 1
}
```

<a id="operacion-073"></a>

## PUT /api/v1/proveedores/{id}

Permiso: **INVENTARIO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "razonSocial": "Proveedor de ejemplo",
  "telefono": "999999999",
  "activo": true,
  "bebidaIds": [
    1
  ]
}
```

Respuesta 200:

```json
{
  "id": 1,
  "razonSocial": "Proveedor de ejemplo",
  "telefono": "999999999",
  "activo": true,
  "bebidaIds": [
    1
  ],
  "version": 1
}
```

<a id="operacion-074"></a>

## DELETE /api/v1/proveedores/{id}

Permiso: **INVENTARIO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
null
```

<a id="operacion-075"></a>

## GET /api/v1/reglas-cobro

Permiso: **MENU_LEER**.

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "id": 1,
    "nombre": "Ejemplo",
    "servicio": "D",
    "precioCombo": 10,
    "recargoLlevar": 10,
    "activo": true,
    "componentes": [
      {
        "categoria": "PAN",
        "cantidad": 1
      }
    ],
    "version": 1
  }
]
```

<a id="operacion-076"></a>

## POST /api/v1/reglas-cobro

Permiso: **MENU_GESTIONAR**.

Request:

```json
{
  "nombre": "Desayuno",
  "servicio": "D",
  "precioCombo": 4.5,
  "recargoLlevar": 0,
  "activo": true,
  "componentes": [
    {
      "categoria": "PAN",
      "cantidad": 2
    },
    {
      "categoria": "BEBIDA_DESAYUNO",
      "cantidad": 1
    }
  ]
}
```

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "servicio": "D",
  "precioCombo": 10,
  "recargoLlevar": 10,
  "activo": true,
  "componentes": [
    {
      "categoria": "PAN",
      "cantidad": 1
    }
  ],
  "version": 1
}
```

<a id="operacion-077"></a>

## GET /api/v1/reglas-cobro/{id}

Permiso: **MENU_LEER**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "servicio": "D",
  "precioCombo": 10,
  "recargoLlevar": 10,
  "activo": true,
  "componentes": [
    {
      "categoria": "PAN",
      "cantidad": 1
    }
  ],
  "version": 1
}
```

<a id="operacion-078"></a>

## PUT /api/v1/reglas-cobro/{id}

Permiso: **MENU_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "nombre": "Desayuno",
  "servicio": "D",
  "precioCombo": 4.5,
  "recargoLlevar": 0,
  "activo": true,
  "componentes": [
    {
      "categoria": "PAN",
      "cantidad": 2
    },
    {
      "categoria": "BEBIDA_DESAYUNO",
      "cantidad": 1
    }
  ]
}
```

Respuesta 200:

```json
{
  "id": 1,
  "nombre": "Ejemplo",
  "servicio": "D",
  "precioCombo": 10,
  "recargoLlevar": 10,
  "activo": true,
  "componentes": [
    {
      "categoria": "PAN",
      "cantidad": 1
    }
  ],
  "version": 1
}
```

<a id="operacion-079"></a>

## DELETE /api/v1/reglas-cobro/{id}

Permiso: **MENU_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
null
```

<a id="operacion-080"></a>

## GET /api/v1/reportes/asistencia-diaria

Permiso: **REPORTE_VER**.

Parámetros: `desde` (query, obligatorio), `hasta` (query, obligatorio), `localId` (query, opcional).

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "fecha": "2026-11-03",
    "servicio": "D",
    "consumos": 1
  }
]
```

<a id="operacion-081"></a>

## GET /api/v1/reportes/contratos-por-vencer

Permiso: **CONTRATO_LEER**.

Parámetros: `dias` (query, opcional, defecto 3), `desde` (query, opcional), `hasta` (query, opcional), `localId` (query, opcional).

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "contratoId": 1,
    "numero": "Ejemplo",
    "dni": "87654321",
    "clienteNombre": "Ejemplo",
    "fechaFin": "2026-11-03",
    "servicio": "D",
    "saldoRaciones": 1
  }
]
```

<a id="operacion-082"></a>

## GET /api/v1/reportes/gastos-vs-ingresos

Permiso: **REPORTE_VER**.

Parámetros: `desde` (query, obligatorio), `hasta` (query, obligatorio), `localId` (query, opcional), `agrupacion` (query, opcional, defecto DIA).

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "periodo": "2026-11",
    "ingresos": 10,
    "gastos": 10,
    "diferencia": 10
  }
]
```

<a id="operacion-083"></a>

## GET /api/v1/reportes/ingresos-mensuales

Permiso: **REPORTE_VER**.

Parámetros: `desde` (query, obligatorio), `hasta` (query, obligatorio), `localId` (query, opcional).

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "periodo": "2026-11",
    "contratos": 10,
    "ventas": 10,
    "total": 10
  }
]
```

<a id="operacion-084"></a>

## GET /api/v1/reportes/platos-mas-consumidos

Permiso: **REPORTE_VER**.

Parámetros: `desde` (query, obligatorio), `hasta` (query, obligatorio), `localId` (query, opcional), `servicio` (query, opcional).

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "platoId": 1,
    "platoNombre": "Ejemplo",
    "servicio": "D",
    "cantidad": 1
  }
]
```

<a id="operacion-085"></a>

## GET /api/v1/reportes/ventas-vs-pensiones

Permiso: **REPORTE_VER**.

Parámetros: `desde` (query, obligatorio), `hasta` (query, obligatorio), `localId` (query, opcional).

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "periodo": "2026-11",
    "contratos": 10,
    "ventas": 10,
    "total": 10
  }
]
```

<a id="operacion-086"></a>

## GET /api/v1/stock-bebidas

Permiso: **INVENTARIO_GESTIONAR**.

Parámetros: `localId` (query, opcional).

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "id": 1,
    "localId": 1,
    "bebidaId": 1,
    "bebidaNombre": "Ejemplo",
    "stock": 1
  }
]
```

<a id="operacion-087"></a>

## GET /api/v1/tarifas

Permiso: **MENU_LEER**.

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "id": 1,
    "servicio": "D",
    "precio": 10,
    "activo": true,
    "version": 1
  }
]
```

<a id="operacion-088"></a>

## POST /api/v1/tarifas

Permiso: **MENU_GESTIONAR**.

Request:

```json
{
  "servicio": "D",
  "precio": 10,
  "activo": true
}
```

Respuesta 200:

```json
{
  "id": 1,
  "servicio": "D",
  "precio": 10,
  "activo": true,
  "version": 1
}
```

<a id="operacion-089"></a>

## GET /api/v1/tarifas/{id}

Permiso: **MENU_LEER**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "servicio": "D",
  "precio": 10,
  "activo": true,
  "version": 1
}
```

<a id="operacion-090"></a>

## PUT /api/v1/tarifas/{id}

Permiso: **MENU_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "servicio": "D",
  "precio": 10,
  "activo": true
}
```

Respuesta 200:

```json
{
  "id": 1,
  "servicio": "D",
  "precio": 10,
  "activo": true,
  "version": 1
}
```

<a id="operacion-091"></a>

## DELETE /api/v1/tarifas/{id}

Permiso: **MENU_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
null
```

<a id="operacion-092"></a>

## GET /api/v1/usuarios

Permiso: **USUARIO_GESTIONAR**.

Request: sin cuerpo.

Respuesta 200:

```json
[
  {
    "id": 1,
    "empleadoId": 1,
    "dni": "87654321",
    "nombre": "Ejemplo",
    "apellido": "Pérez",
    "telefono": "999999999",
    "localId": 1,
    "localNombre": "Ejemplo",
    "cargo": "Ejemplo",
    "correo": "admin@mana.pe",
    "rol": "ADMINISTRADOR",
    "activo": true,
    "version": 1,
    "gestionable": true
  }
]
```

<a id="operacion-093"></a>

## POST /api/v1/usuarios

Permiso: **USUARIO_GESTIONAR**.

Request:

```json
{
  "dni": "87654321",
  "nombre": "Ejemplo",
  "apellido": "Pérez",
  "telefono": "999999999",
  "localId": 1,
  "cargo": "Ejemplo",
  "correo": "admin@mana.pe",
  "clave": "<contraseña>",
  "rol": "ADMINISTRADOR",
  "activo": true
}
```

Respuesta 200:

```json
{
  "id": 1,
  "empleadoId": 1,
  "dni": "87654321",
  "nombre": "Ejemplo",
  "apellido": "Pérez",
  "telefono": "999999999",
  "localId": 1,
  "localNombre": "Ejemplo",
  "cargo": "Ejemplo",
  "correo": "admin@mana.pe",
  "rol": "ADMINISTRADOR",
  "activo": true,
  "version": 1,
  "gestionable": true
}
```

<a id="operacion-094"></a>

## GET /api/v1/usuarios/{id}

Permiso: **USUARIO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "empleadoId": 1,
  "dni": "87654321",
  "nombre": "Ejemplo",
  "apellido": "Pérez",
  "telefono": "999999999",
  "localId": 1,
  "localNombre": "Ejemplo",
  "cargo": "Ejemplo",
  "correo": "admin@mana.pe",
  "rol": "ADMINISTRADOR",
  "activo": true,
  "version": 1,
  "gestionable": true
}
```

<a id="operacion-095"></a>

## PUT /api/v1/usuarios/{id}

Permiso: **USUARIO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "dni": "87654321",
  "nombre": "Ejemplo",
  "apellido": "Pérez",
  "telefono": "999999999",
  "localId": 1,
  "cargo": "Ejemplo",
  "correo": "admin@mana.pe",
  "clave": "<contraseña>",
  "rol": "ADMINISTRADOR",
  "activo": true
}
```

Respuesta 200:

```json
{
  "id": 1,
  "empleadoId": 1,
  "dni": "87654321",
  "nombre": "Ejemplo",
  "apellido": "Pérez",
  "telefono": "999999999",
  "localId": 1,
  "localNombre": "Ejemplo",
  "cargo": "Ejemplo",
  "correo": "admin@mana.pe",
  "rol": "ADMINISTRADOR",
  "activo": true,
  "version": 1,
  "gestionable": true
}
```

<a id="operacion-096"></a>

## DELETE /api/v1/usuarios/{id}

Permiso: **USUARIO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
null
```

<a id="operacion-097"></a>

## PATCH /api/v1/usuarios/{id}/rol

Permiso: **USUARIO_GESTIONAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "rol": "ADMINISTRADOR"
}
```

Respuesta 200:

```json
{
  "id": 1,
  "empleadoId": 1,
  "dni": "87654321",
  "nombre": "Ejemplo",
  "apellido": "Pérez",
  "telefono": "999999999",
  "localId": 1,
  "localNombre": "Ejemplo",
  "cargo": "Ejemplo",
  "correo": "admin@mana.pe",
  "rol": "ADMINISTRADOR",
  "activo": true,
  "version": 1,
  "gestionable": true
}
```

<a id="operacion-098"></a>

## GET /api/v1/ventas

Permiso: **VENTA_REGISTRAR**.

Parámetros: `desde` (query, opcional), `hasta` (query, opcional), `estado` (query, opcional), `cajaId` (query, opcional), `localId` (query, opcional), `pagina` (query, opcional, defecto 0), `tamanio` (query, opcional, defecto 20), `ordenarPor` (query, opcional, defecto id), `direccion` (query, opcional, defecto desc).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "contenido": [
    {
      "id": 1,
      "numero": "Ejemplo",
      "localId": 1,
      "cajaId": 1,
      "clienteId": 1,
      "servicio": "D",
      "paraLlevar": true,
      "estado": "PENDIENTE",
      "fecha": "2026-11-03T12:00:00",
      "recargoLlevar": 10,
      "total": 10,
      "anuladaEn": "2026-11-03T12:00:00",
      "motivoAnulacion": "Ejemplo",
      "lineas": [
        {
          "id": 1,
          "tipo": "PLATO",
          "platoId": 1,
          "reglaId": 1,
          "bebidaId": 1,
          "descripcion": "Descripción de ejemplo",
          "cantidad": 1,
          "precioUnitario": 10,
          "recargoUnitario": 10,
          "platos": [
            {
              "platoId": 1,
              "nombre": "Ejemplo",
              "cantidad": 1
            }
          ]
        }
      ],
      "pagos": [
        {
          "id": 1,
          "metodo": "YAPE",
          "monto": 10,
          "fecha": "2026-11-03T12:00:00"
        }
      ],
      "devoluciones": [
        {
          "id": 1,
          "metodo": "YAPE",
          "monto": 10,
          "fecha": "2026-11-03T12:00:00",
          "motivo": "Corrección solicitada por el cliente"
        }
      ],
      "version": 1
    }
  ],
  "pagina": 0,
  "tamanio": 20,
  "totalElementos": 1,
  "totalPaginas": 1,
  "ultima": true
}
```

<a id="operacion-099"></a>

## POST /api/v1/ventas

Permiso: **VENTA_REGISTRAR**.

Request:

```json
{
  "servicio": "A",
  "paraLlevar": true,
  "lineas": [
    {
      "tipo": "BEBIDA",
      "cantidad": 2,
      "bebidaId": 1
    }
  ]
}
```

Respuesta 200:

```json
{
  "id": 1,
  "numero": "Ejemplo",
  "localId": 1,
  "cajaId": 1,
  "clienteId": 1,
  "servicio": "D",
  "paraLlevar": true,
  "estado": "PENDIENTE",
  "fecha": "2026-11-03T12:00:00",
  "recargoLlevar": 10,
  "total": 10,
  "anuladaEn": "2026-11-03T12:00:00",
  "motivoAnulacion": "Ejemplo",
  "lineas": [
    {
      "id": 1,
      "tipo": "PLATO",
      "platoId": 1,
      "reglaId": 1,
      "bebidaId": 1,
      "descripcion": "Descripción de ejemplo",
      "cantidad": 1,
      "precioUnitario": 10,
      "recargoUnitario": 10,
      "platos": [
        {
          "platoId": 1,
          "nombre": "Ejemplo",
          "cantidad": 1
        }
      ]
    }
  ],
  "pagos": [
    {
      "id": 1,
      "metodo": "YAPE",
      "monto": 10,
      "fecha": "2026-11-03T12:00:00"
    }
  ],
  "devoluciones": [
    {
      "id": 1,
      "metodo": "YAPE",
      "monto": 10,
      "fecha": "2026-11-03T12:00:00",
      "motivo": "Corrección solicitada por el cliente"
    }
  ],
  "version": 1
}
```

<a id="operacion-100"></a>

## GET /api/v1/ventas/{id}

Permiso: **VENTA_REGISTRAR**.

Parámetros: `id` (path, obligatorio).

Request: sin cuerpo.

Respuesta 200:

```json
{
  "id": 1,
  "numero": "Ejemplo",
  "localId": 1,
  "cajaId": 1,
  "clienteId": 1,
  "servicio": "D",
  "paraLlevar": true,
  "estado": "PENDIENTE",
  "fecha": "2026-11-03T12:00:00",
  "recargoLlevar": 10,
  "total": 10,
  "anuladaEn": "2026-11-03T12:00:00",
  "motivoAnulacion": "Ejemplo",
  "lineas": [
    {
      "id": 1,
      "tipo": "PLATO",
      "platoId": 1,
      "reglaId": 1,
      "bebidaId": 1,
      "descripcion": "Descripción de ejemplo",
      "cantidad": 1,
      "precioUnitario": 10,
      "recargoUnitario": 10,
      "platos": [
        {
          "platoId": 1,
          "nombre": "Ejemplo",
          "cantidad": 1
        }
      ]
    }
  ],
  "pagos": [
    {
      "id": 1,
      "metodo": "YAPE",
      "monto": 10,
      "fecha": "2026-11-03T12:00:00"
    }
  ],
  "devoluciones": [
    {
      "id": 1,
      "metodo": "YAPE",
      "monto": 10,
      "fecha": "2026-11-03T12:00:00",
      "motivo": "Corrección solicitada por el cliente"
    }
  ],
  "version": 1
}
```

<a id="operacion-101"></a>

## PATCH /api/v1/ventas/{id}/anular

Permiso: **VENTA_ANULAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "motivo": "Corrección solicitada por el cliente"
}
```

Respuesta 200:

```json
{
  "id": 1,
  "numero": "Ejemplo",
  "localId": 1,
  "cajaId": 1,
  "clienteId": 1,
  "servicio": "D",
  "paraLlevar": true,
  "estado": "PENDIENTE",
  "fecha": "2026-11-03T12:00:00",
  "recargoLlevar": 10,
  "total": 10,
  "anuladaEn": "2026-11-03T12:00:00",
  "motivoAnulacion": "Ejemplo",
  "lineas": [
    {
      "id": 1,
      "tipo": "PLATO",
      "platoId": 1,
      "reglaId": 1,
      "bebidaId": 1,
      "descripcion": "Descripción de ejemplo",
      "cantidad": 1,
      "precioUnitario": 10,
      "recargoUnitario": 10,
      "platos": [
        {
          "platoId": 1,
          "nombre": "Ejemplo",
          "cantidad": 1
        }
      ]
    }
  ],
  "pagos": [
    {
      "id": 1,
      "metodo": "YAPE",
      "monto": 10,
      "fecha": "2026-11-03T12:00:00"
    }
  ],
  "devoluciones": [
    {
      "id": 1,
      "metodo": "YAPE",
      "monto": 10,
      "fecha": "2026-11-03T12:00:00",
      "motivo": "Corrección solicitada por el cliente"
    }
  ],
  "version": 1
}
```

<a id="operacion-102"></a>

## POST /api/v1/ventas/{id}/pagos

Permiso: **VENTA_REGISTRAR**.

Parámetros: `id` (path, obligatorio).

Request:

```json
{
  "pagos": [
    {
      "metodo": "YAPE",
      "monto": 10
    }
  ]
}
```

Respuesta 200:

```json
{
  "id": 1,
  "numero": "Ejemplo",
  "localId": 1,
  "cajaId": 1,
  "clienteId": 1,
  "servicio": "D",
  "paraLlevar": true,
  "estado": "PENDIENTE",
  "fecha": "2026-11-03T12:00:00",
  "recargoLlevar": 10,
  "total": 10,
  "anuladaEn": "2026-11-03T12:00:00",
  "motivoAnulacion": "Ejemplo",
  "lineas": [
    {
      "id": 1,
      "tipo": "PLATO",
      "platoId": 1,
      "reglaId": 1,
      "bebidaId": 1,
      "descripcion": "Descripción de ejemplo",
      "cantidad": 1,
      "precioUnitario": 10,
      "recargoUnitario": 10,
      "platos": [
        {
          "platoId": 1,
          "nombre": "Ejemplo",
          "cantidad": 1
        }
      ]
    }
  ],
  "pagos": [
    {
      "id": 1,
      "metodo": "YAPE",
      "monto": 10,
      "fecha": "2026-11-03T12:00:00"
    }
  ],
  "devoluciones": [
    {
      "id": 1,
      "metodo": "YAPE",
      "monto": 10,
      "fecha": "2026-11-03T12:00:00",
      "motivo": "Corrección solicitada por el cliente"
    }
  ],
  "version": 1
}
```
