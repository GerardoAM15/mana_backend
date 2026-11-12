import fs from 'node:fs';

const api=JSON.parse(fs.readFileSync('target/openapi.json','utf8'));
api.servers=[{url:'http://localhost:8080',description:'Desarrollo local'}];
const resolve=s=>s?.$ref?api.components.schemas[s.$ref.split('/').at(-1)]:s;
const fecha='2026-11-03';
function ejemplo(schema,name='',depth=0){
 const s=resolve(schema);if(!s || depth>9)return null;
 if(s.example!==undefined)return s.example;
 if(s.enum?.length)return s.enum[0];
 if(s.default!==undefined)return s.default;
 if(s.allOf)return Object.assign({},...s.allOf.map(x=>ejemplo(x,name,depth+1)));
 if(s.oneOf || s.anyOf)return ejemplo((s.oneOf||s.anyOf)[0],name,depth+1);
 if(s.type==='array')return [ejemplo(s.items,name,depth+1)];
 if(s.properties)return Object.fromEntries(Object.entries(s.properties).map(([k,v])=>[k,ejemplo(v,k,depth+1)]));
 if(s.type==='boolean')return true;
 if(s.type==='integer')return name==='pagina'?0:name==='tamanio'?20:1;
 if(s.type==='number')return 10;
 if(s.format==='date')return fecha;
 if(s.format==='date-time')return fecha+'T12:00:00';
 const values={correo:'admin@mana.pe',clave:'<contraseña>',nuevaClave:'<nueva contraseña>',dni:'87654321',nombre:'Ejemplo',apellido:'Pérez',razonSocial:'Proveedor de ejemplo',servicio:'D',categoria:'PAN',metodo:'YAPE',tipo:'Bearer',rol:'CAJERO',motivo:'Corrección solicitada por el cliente',descripcion:'Descripción de ejemplo',token:'<JWT>',referencia:'OP-001',periodo:'2026-11',direccion:'Dirección del local',telefono:'999999999',message:'Datos inválidos',path:'/api/v1/clientes',error:'Bad Request'};
 return values[name]||'Ejemplo';
}
function permiso(method,path){
 if(path==='/api/health'||path.match(/\/auth\/(login|recuperar|restablecer)$/))return 'Público';
 if(path.match(/\/auth\/me$|\/catalogos$/))return 'Autenticado';
 if(path.startsWith('/api/v1/comprobantes'))return path.endsWith('/reenviar')?'COMPROBANTE_GESTIONAR':method==='get'?'COMPROBANTE_VER':'COMPROBANTE_EMITIR';
 if(path.includes('/usuarios'))return 'USUARIO_GESTIONAR';if(path.includes('/locales'))return 'LOCAL_GESTIONAR';
 if(path.includes('/contrato-vigente')||path.includes('/pensionistas'))return 'CONTRATO_LEER';
 if(path.includes('/clientes'))return method==='get'?'CLIENTE_LEER':'CLIENTE_ESCRIBIR';
 if(path.includes('/contratos'))return method==='get'?'CONTRATO_LEER':path.endsWith('/anular')?'CONTRATO_ANULAR':'CONTRATO_REGISTRAR';
 if(path.includes('/consumos'))return method==='get'?'CONTRATO_LEER':path.endsWith('/anular')?'CONSUMO_ANULAR':'CONSUMO_REGISTRAR';
 if(path.includes('/ventas')&&!path.includes('/reportes'))return path.endsWith('/anular')?'VENTA_ANULAR':'VENTA_REGISTRAR';
 if(path.includes('/pos'))return 'VENTA_REGISTRAR';
 if(path.includes('/cajas'))return method==='get'&&path==='/api/v1/cajas'?'CAJA_SUPERVISAR':'CAJA_OPERAR';
 if(path.includes('/reportes'))return 'REPORTE_VER';if(path.includes('/planes'))return 'PLAN_GESTIONAR';
 if(path.includes('/dias-no-atencion'))return 'CALENDARIO_GESTIONAR';if(path.includes('/gastos'))return 'GASTO_GESTIONAR';
 if(path.match(/\/(bebidas|proveedores|compras-bebidas|stock-bebidas|movimientos-stock)/))return 'INVENTARIO_GESTIONAR';
 return method==='get'?'MENU_LEER':'MENU_GESTIONAR';
}
const overrides={
 '/api/v1/auth/login':{correo:'{{correo}}',clave:'{{clave}}'},
 '/api/v1/auth/recuperar':{correo:'{{correo}}'},
 '/api/v1/auth/restablecer':{token:'{{tokenRestablecimiento}}',nuevaClave:'{{nuevaClave}}'},
 '/api/v1/contratos/simular':{clienteId:1,planId:1,fechaInicio:fecha,servicios:['D']},
 '/api/v1/contratos':{clienteId:1,planId:1,fechaInicio:fecha,servicios:['D'],pagos:[{metodo:'YAPE',monto:16,referencia:'OP-001'}]},
 '/api/v1/contratos/{id}/renovar':{planId:1,servicios:['D'],pagos:[{metodo:'YAPE',monto:16}]},
 '/api/v1/consumos':{contratoId:1,servicio:'D',platos:[{platoId:1,cantidad:2},{platoId:2,cantidad:1}],paraLlevar:false},
 '/api/v1/ventas':{servicio:'A',paraLlevar:true,lineas:[{tipo:'BEBIDA',cantidad:2,bebidaId:1}]},
 '/api/v1/ventas/{id}/pagos':{pagos:[{metodo:'YAPE',monto:10}]},
 '/api/v1/cajas/abrir':{montoInicial:50},
 '/api/v1/cajas/{id}/cerrar':{conteos:[{metodo:'EFECTIVO',montoContado:50},{metodo:'YAPE',montoContado:0}],observacion:'Arqueo de cierre'},
 '/api/v1/menus':{fecha,servicio:'D',platoIds:[1,2]},
 '/api/v1/menus/clonar':{fechaOrigen:fecha,fechaDestino:'2026-11-04',servicios:['D']},
 '/api/v1/menus/copiar-a-cena':{fecha},
 '/api/v1/reglas-cobro':{nombre:'Desayuno',servicio:'D',precioCombo:4.5,recargoLlevar:0,activo:true,componentes:[{categoria:'PAN',cantidad:2},{categoria:'BEBIDA_DESAYUNO',cantidad:1}]},
 '/api/v1/precios-sueltos':{servicio:'D',categoria:'PAN',precio:1.5,recargoLlevar:0,activo:true},
 '/api/v1/planes':{nombre:'Semanal',periodo:'SEMANAL',activo:true,servicios:['D','A','C']},
 '/api/v1/proveedores':{razonSocial:'Proveedor de ejemplo',telefono:'999999999',activo:true,bebidaIds:[1]},
 '/api/v1/compras-bebidas':{proveedorId:1,fecha,detalles:[{bebidaId:1,presentaciones:2,unidadesPorPresentacion:6,costoTotal:18}]}
};
const collection={info:{name:'MANÁ — API completa',schema:'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',description:'Generada desde docs/openapi.json. Configure correo, clave, fechas e ids reales. Login guarda token. Los ejemplos de dinero se ajustan a las tarifas vigentes; simular actualiza montoContrato. Las escrituras y eliminaciones tienen efectos reales: ejecutar cada operación elegida, no toda la colección en lote.'},auth:{type:'bearer',bearer:[{key:'token',value:'{{token}}',type:'string'}]},variable:[],item:[]};
const vars={baseUrl:'http://localhost:8080',token:'',correo:'admin@mana.pe',clave:'',nuevaClave:'',tokenRestablecimiento:'',hoy:'',desde:fecha,hasta:'2026-11-30',clienteId:1,contratoId:1,ventaId:1,cajaId:1,platoId:1,panId:1,platoBebidaId:2,planId:1,bebidaId:1,proveedorId:1,usuarioId:1,localId:1,consumoId:1,compraId:1,gastoId:1,reglaId:1,tarifaId:1,diaId:1,servicio:'D',categoria:'PAN',montoContrato:16,montoVenta:10};
collection.variable=Object.entries(vars).map(([key,value])=>({key,value:String(value),type:'string'}));
collection.event=[{listen:'prerequest',script:{type:'text/javascript',exec:[`if(!pm.collectionVariables.get('hoy')){const partes=new Intl.DateTimeFormat('en-CA',{timeZone:'America/Lima',year:'numeric',month:'2-digit',day:'2-digit'}).formatToParts(new Date());const p=Object.fromEntries(partes.map(x=>[x.type,x.value]));pm.collectionVariables.set('hoy',p.year+'-'+p.month+'-'+p.day);}`]}}];
const folders=new Map();let table='| Método | Ruta | Permiso | Ejemplos |\n|---|---|---|---|\n';let details='# Contrato HTTP de MANÁ\n\nGenerado desde el OpenAPI real. Los ejemplos son ilustrativos: use ids existentes y precios de la BD. Los listados paginados contienen `contenido`, `pagina`, `tamanio`, `totalElementos`, `totalPaginas` y `ultima`. `desde`/`hasta` son inclusivos; una página empieza en 0.\n\n';let count=0;
for(const [path,operations]of Object.entries(api.paths).sort(([a],[b])=>a.localeCompare(b)))for(const [method,op]of Object.entries(operations)){
 if(!['get','post','put','patch','delete'].includes(method))continue;
 const permission=permiso(method,path);const id='operacion-'+String(++count).padStart(3,'0');
 const module=path.split('/')[3]||'health';if(!folders.has(module))folders.set(module,{name:module,item:[]});
 const schema=op.requestBody?.content?.['application/json']?.schema;
 const body=schema?(overrides[path]||overrides[path.replace(/\/\{id\}$/,'')]||ejemplo(schema)):undefined;
 const code=Object.keys(op.responses||{}).find(x=>/^2\d\d$/.test(x))||'200';const content=op.responses?.[code]?.content;const responseSchema=content?.['application/json']?.schema||Object.values(content||{})[0]?.schema;
 let response=code==='204'?undefined:ejemplo(responseSchema);
 if(path==='/api/health')response='Backend funcionando correctamente';
 if(path==='/api/v1/auth/login' && response){
  const permisoSrc=fs.readFileSync('src/main/java/pe/edu/upeu/mana/enums/Permiso.java','utf8');
  response.tipo='Bearer';response.expiraEnSegundos=3600;response.rol='DUENO';response.nombreCompleto='Dueño MANÁ';
  response.permisos=permisoSrc.slice(permisoSrc.indexOf('{')+1,permisoSrc.lastIndexOf('}')).match(/\b[A-Z][A-Z_]+\b/g)||[];
 }
 if(response && typeof response==='object' && !Array.isArray(response) && response.contenido){response.pagina=0;response.tamanio=20;response.totalElementos=response.contenido.length;response.totalPaginas=1;response.ultima=true;}
 table+=`| ${method.toUpperCase()} | \`${path}\` | ${permission} | [Request / response](docs/ENDPOINTS.md#${id}) |\n`;
 details+=`<a id="${id}"></a>\n\n## ${method.toUpperCase()} ${path}\n\nPermiso: **${permission}**.\n\n`;
 if(op.parameters?.length)details+='Parámetros: '+op.parameters.map(p=>`\`${p.name}\` (${p.in}${p.required?', obligatorio':', opcional'}${p.schema?.default!==undefined?', defecto '+p.schema.default:''})`).join(', ')+'.\n\n';
 details+=body!==undefined?'Request:\n\n```json\n'+JSON.stringify(body,null,2)+'\n```\n\n':'Request: sin cuerpo.\n\n';
 details+=`Respuesta ${code}:\n\n`+(response===undefined?'Sin contenido.\n\n':path==='/api/health'?'```text\n'+response+'\n```\n\n':'```json\n'+JSON.stringify(response,null,2)+'\n```\n\n');
 const prefix=path.split('/')[3];const map={clientes:'clienteId',contratos:'contratoId',ventas:'ventaId',cajas:'cajaId',platos:'platoId',planes:'planId',bebidas:'bebidaId',proveedores:'proveedorId',usuarios:'usuarioId',locales:'localId',consumos:'consumoId','compras-bebidas':'compraId',gastos:'gastoId','reglas-cobro':'reglaId',tarifas:'tarifaId','dias-no-atencion':'diaId'};
 let url='{{baseUrl}}'+path.replace(/\{(\w+)\}/g,(_,k)=>'{{'+(k==='id'?(map[prefix]||'id'):k)+'}}');
 const params=(op.parameters||[]).filter(p=>p.in==='query').map(p=>({key:p.name,value:String(p.schema?.default??(['desde','hasta','localId'].includes(p.name)?'{{'+p.name+'}}':p.name==='q'?'87654321':ejemplo(p.schema,p.name))),disabled:!p.required && p.schema?.default===undefined}));
 const request={method:method.toUpperCase(),header:[],url:{raw:url+(params.some(p=>!p.disabled)?'?'+params.filter(p=>!p.disabled).map(p=>p.key+'='+p.value).join('&'):''),host:['{{baseUrl}}'],path:url.slice('{{baseUrl}}/'.length).split('/'),query:params},description:`Permiso: ${permission}. ${op.description||''}`};
 if(permission==='Público')request.auth={type:'noauth'};
 if(body!==undefined){let raw=JSON.stringify(body,null,2);if(!path.startsWith('/api/v1/auth')){
  raw=raw.replace(new RegExp(fecha,'g'),'{{hoy}}');
  for(const k of ['clienteId','contratoId','planId','bebidaId','proveedorId','localId'])raw=raw.replace(new RegExp('"'+k+'": 1','g'),'"'+k+'": {{'+k+'}}');
  if(path.includes('/contratos')&&path!='/api/v1/contratos/simular')raw=raw.replace('"monto": 16','"monto": {{montoContrato}}');
  if(path==='/api/v1/ventas/{id}/pagos')raw=raw.replace('"monto": 10','"monto": {{montoVenta}}');
 }
 request.header=[{key:'Content-Type',value:'application/json'}];request.body={mode:'raw',raw,options:{raw:{language:'json'}}};}
 const item={name:method.toUpperCase()+' '+path,request,response:[],event:[]};
 if(response!==undefined)item.response.push({name:'Ejemplo '+code,originalRequest:request,status:code==='201'?'Created':'OK',code:Number(code),header:[{key:'Content-Type',value:path==='/api/health'?'text/plain':'application/json'}],body:path==='/api/health'?response:JSON.stringify(response,null,2)});
 const scripts=[`pm.test('Estado HTTP esperado',()=>pm.expect(pm.response.code).to.eql(${Number(code)}));`];
 if(path==='/api/v1/auth/login')scripts.push(`if(pm.response.code===200)pm.collectionVariables.set('token',pm.response.json().token);`);
 if(path==='/api/v1/contratos/simular')scripts.push(`if(pm.response.code===200)pm.collectionVariables.set('montoContrato',pm.response.json().total);`);
 if(method==='post' && map[prefix] && !path.endsWith('/anular'))scripts.push(`if(pm.response.code===201){const d=pm.response.json();if(d.id)pm.collectionVariables.set('${map[prefix]}',d.id);${prefix==='ventas'?"pm.collectionVariables.set('montoVenta',d.total);":''}}`);
 item.event.push({listen:'test',script:{type:'text/javascript',exec:scripts}});folders.get(module).item.push(item);
}
collection.item=[...folders.values()];
fs.mkdirSync('postman',{recursive:true});fs.mkdirSync('docs',{recursive:true});
fs.writeFileSync('docs/openapi.json',JSON.stringify(api,null,2)+'\n');
fs.writeFileSync('docs/ENDPOINTS.md',details.trimEnd()+'\n');
fs.writeFileSync('postman/MANA.postman_collection.json',JSON.stringify(collection,null,2)+'\n');
const readme=fs.readFileSync('README.md','utf8').split('<!-- ENDPOINTS -->')[0];fs.writeFileSync('README.md',readme+'<!-- ENDPOINTS -->\n\n'+table);
const expected=Object.values(api.paths).reduce((n,p)=>n+Object.keys(p).filter(m=>['get','post','put','patch','delete'].includes(m)).length,0);
if(expected!==count)throw Error('Faltan operaciones');
const valores=Object.fromEntries(collection.variable.map(v=>[v.key,v.value]));
for(const folder of collection.item)for(const item of folder.item){
 if(item.request.body){
  const resuelto=item.request.body.raw.replace(/\{\{(\w+)\}\}/g,(_,key)=>{if(!(key in valores))throw Error('Variable no declarada: '+key);return valores[key];});
  JSON.parse(resuelto);
 }
}
function validarReferencias(value){
 if(!value||typeof value!=='object')return;
 if(value.$ref?.startsWith('#/components/schemas/')&&!resolve(value))throw Error('Referencia OpenAPI ausente: '+value.$ref);
 for(const v of Object.values(value))if(v&&typeof v==='object')validarReferencias(v);
}
validarReferencias(api);
console.log(`Documentadas ${count} operaciones; colección Postman y OpenAPI JSON válidos.`);
