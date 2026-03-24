-- inv_kardex --
CREATE TABLE public.inv_kardex  ( 
	kar_id      	serial NOT NULL,
	kar_producto	integer NULL,
	kar_fecha   	timestamp NULL DEFAULT now(),
	kar_actual  	numeric(15,2) NULL,
	kar_ingreso 	numeric(15,2) NULL,
	kar_salida  	numeric(15,2) NULL,
	kar_total   	numeric(15,2) NULL,
	kar_estado  	varchar(1) NULL 
	);
ALTER TABLE public.inv_kardex OWNER TO postgres;

-- inv_producto --
CREATE TABLE public.inv_producto  ( 
	pro_id    	serial NOT NULL,
	pro_nombre	varchar(30) NULL,
	pro_precio	numeric(15,2) NULL,
	pro_unidad	integer NULL,
	pro_estado	varchar(1) NULL 
	);
ALTER TABLE public.inv_producto OWNER TO postgres;

-- inv_unidad --
CREATE TABLE public.inv_unidad  ( 
	uni_id         	serial NOT NULL,
	uni_nombre     	varchar(50) NULL,
	uni_abreviatura	varchar(25) NULL,
	uni_estado     	varchar(1) NULL 
	);
ALTER TABLE public.inv_unidad OWNER TO postgres;

-- v_inv_producto --
CREATE TABLE public.v_inv_producto  ( 
	pro_id         	integer NULL,
	pro_nombre     	varchar(30) NULL,
	pro_precio     	numeric(15,2) NULL,
	pro_unidad     	integer NULL,
	uni_nombre     	varchar(50) NULL,
	uni_abreviatura	varchar(25) NULL,
	kar_fecha      	timestamp NULL,
	kar_total      	numeric NULL 
	);
ALTER TABLE public.v_inv_producto OWNER TO postgres;

