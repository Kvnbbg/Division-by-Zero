/* TDAAH convertisseur JS © Kevin Marville */
const DIM={L:[1,0,0,0,0],M:[0,1,0,0,0],T:[0,0,1,0,0],I:[0,0,0,1,0],Th:[0,0,0,0,1],area:[2,0,0,0,0],vol:[3,0,0,0,0],speed:[1,0,-1,0,0],energy:[2,1,-2,0,0],power:[2,1,-3,0,0],pressure:[-1,1,-2,0,0],force:[1,1,-2,0,0],none:[0,0,0,0,0]};
const sameDim=(a,b)=>a.every((v,i)=>v===b[i]);
const addDim=(a,b)=>a.map((v,i)=>v+b[i]);
const subDim=(a,b)=>a.map((v,i)=>v-b[i]);
const UNITS=[["m",DIM.L,1],["km",DIM.L,1000],["cm",DIM.L,.01],["mm",DIM.L,.001],["in",DIM.L,.0254],["ft",DIM.L,.3048],["mi",DIM.L,1609.344],["kg",DIM.M,1],["g",DIM.M,.001],["t",DIM.M,1000],["lb",DIM.M,.45359237],["s",DIM.T,1],["min",DIM.T,60],["h",DIM.T,3600],["K",DIM.Th,1,0],["C",DIM.Th,1,273.15],["F",DIM.Th,5/9,255.37222222222223],["m2",DIM.area,1],["ha",DIM.area,10000],["m3",DIM.vol,1],["L",DIM.vol,.001],["mL",DIM.vol,1e-6],["m/s",DIM.speed,1],["km/h",DIM.speed,1000/3600],["mph",DIM.speed,1609.344/3600],["J",DIM.energy,1],["kWh",DIM.energy,3.6e6],["cal",DIM.energy,4.184],["W",DIM.power,1],["kW",DIM.power,1000],["Pa",DIM.pressure,1],["bar",DIM.pressure,1e5],["atm",DIM.pressure,101325],["psi",DIM.pressure,6894.757293168361],["N",DIM.force,1],["A",DIM.I,1]];
const BY={};for(const [symbol,dim,factor,offset=0] of UNITS){BY[symbol.toLowerCase()]={symbol,dim,factor,offset}}
class UnitError extends Error{}
class ZeroDivisionMeasurementError extends Error{constructor(msg="division par zéro (dénominateur de mesure nul)."){super(msg);this.name="ZeroDivisionMeasurementError"}}
function lookup(name){const u=BY[String(name).trim().toLowerCase()];if(!u)throw new UnitError(`unité inconnue : ${name}`);return u}
function toSI(value,unit){return value*unit.factor+unit.offset}
function fromSI(si,unit){return (si-unit.offset)/unit.factor}
function clean(n){if(!Number.isFinite(n))return n;if(Math.abs(n)<1e-12)return 0;const nearest=Math.round(n);if(nearest!==0&&Math.abs(n-nearest)<1e-10)return nearest;return n}
function convert(value,src,dst){const a=lookup(src),b=lookup(dst);if(!sameDim(a.dim,b.dim))throw new UnitError(`conversion impossible : ${a.symbol} vers ${b.symbol}.`);return{value:clean(fromSI(toSI(Number(value),a),b)),unit:b.symbol}}
const SI_FOR={[DIM.L.join()]:"m",[DIM.M.join()]:"kg",[DIM.T.join()]:"s",[DIM.I.join()]:"A",[DIM.Th.join()]:"K",[DIM.area.join()]:"m2",[DIM.vol.join()]:"L",[DIM.speed.join()]:"km/h",[DIM.energy.join()]:"J",[DIM.power.join()]:"W",[DIM.pressure.join()]:"Pa",[DIM.force.join()]:"N",[DIM.none.join()]:""};
function measure(value,unitName){return{value:Number(value),unit:lookup(unitName)}}
function siOf(m){return toSI(m.value,m.unit)}
function unitForDim(dim){const symbol=SI_FOR[dim.join()]||"1";return BY[symbol.toLowerCase()]||{symbol,dim,factor:1,offset:0}}
function calculate(expr){const tokens=String(expr).trim().match(/[+-]?(?:\d+(?:\.\d*)?|\.\d+)(?:e[+-]?\d+)?|[A-Za-zµ°Ω][A-Za-z0-9µ°Ω/\u00b2\u00b3]*|[()+\-*/]/gi);if(!tokens)throw new UnitError("expression vide.");const merged=[];for(let i=0;i<tokens.length;i+=1){const t=tokens[i];if(/^[+-]?(?:\d|\.)/.test(t)&&i+1<tokens.length&&/^[A-Za-zµ°Ω]/.test(tokens[i+1])){merged.push(measure(t,tokens[i+1]));i+=1}else if(/^[+-]?(?:\d|\.)/.test(t)){merged.push({value:Number(t),unit:{symbol:"1",dim:DIM.none,factor:1,offset:0}})}else merged.push(t)}
const add=(x,y)=>{if(!sameDim(x.unit.dim,y.unit.dim))throw new UnitError("dimensions incompatibles pour +/\u2212.");return{value:fromSI(siOf(x)+siOf(y),x.unit),unit:x.unit}};
const sub=(x,y)=>add(x,{value:-y.value,unit:y.unit});
const mul=(x,y)=>{const unit=unitForDim(addDim(x.unit.dim,y.unit.dim));return{value:fromSI(siOf(x)*siOf(y),unit),unit}};
const div=(x,y)=>{if(siOf(y)===0)throw new ZeroDivisionMeasurementError();const unit=unitForDim(subDim(x.unit.dim,y.unit.dim));return{value:fromSI(siOf(x)/siOf(y),unit),unit}};
function parseExpr(pos){let [left,i]=parseTerm(pos);while(i<merged.length&&(merged[i]==="+"||merged[i]==="-")){const op=merged[i];const [right,j]=parseTerm(i+1);left=op==="+"?add(left,right):sub(left,right);i=j}return[left,i]}
function parseTerm(pos){let [left,i]=parseFactor(pos);while(i<merged.length&&(merged[i]==="*"||merged[i]==="/")){const op=merged[i];const [right,j]=parseFactor(i+1);left=op==="*"?mul(left,right):div(left,right);i=j}return[left,i]}
function parseFactor(pos){if(pos>=merged.length)throw new UnitError("expression incomplète.");const tok=merged[pos];if(tok==="+")return parseFactor(pos+1);if(tok==="-"){const [v,i]=parseFactor(pos+1);return[{value:-v.value,unit:v.unit},i]}if(tok==="("){const [v,i]=parseExpr(pos+1);if(merged[i]!==")")throw new UnitError("parenthèse fermante manquante.");return[v,i+1]}if(tok&&typeof tok==="object")return[tok,pos+1];throw new UnitError(`facteur inattendu : ${tok}`)}
const [result,end]=parseExpr(0);if(end!==merged.length)throw new UnitError("expression mal formée.");return{value:clean(result.value),unit:result.unit.symbol}}
function unitSymbols(){return UNITS.map(u=>u[0])}
window.TDAAH={convert,calculate,unitSymbols,UnitError,ZeroDivisionMeasurementError};
