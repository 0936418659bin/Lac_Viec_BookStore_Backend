--
-- PostgreSQL database dump
--

-- Dumped from database version 15.13 (Debian 15.13-1.pgdg120+1)
-- Dumped by pg_dump version 15.13 (Debian 15.13-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: bookstore; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA bookstore;


ALTER SCHEMA bookstore OWNER TO postgres;

--
-- Name: update_products_updated_at(); Type: FUNCTION; Schema: bookstore; Owner: postgres
--

CREATE FUNCTION bookstore.update_products_updated_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;


ALTER FUNCTION bookstore.update_products_updated_at() OWNER TO postgres;

--
-- Name: update_updated_at_column(); Type: FUNCTION; Schema: bookstore; Owner: postgres
--

CREATE FUNCTION bookstore.update_updated_at_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;


ALTER FUNCTION bookstore.update_updated_at_column() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: addresses; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.addresses (
    id integer NOT NULL,
    user_id integer,
    address_line character varying(255) NOT NULL,
    city character varying(100),
    district character varying(100),
    ward character varying(100),
    phone character varying(20),
    is_default boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE bookstore.addresses OWNER TO postgres;

--
-- Name: addresses_id_seq; Type: SEQUENCE; Schema: bookstore; Owner: postgres
--

CREATE SEQUENCE bookstore.addresses_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE bookstore.addresses_id_seq OWNER TO postgres;

--
-- Name: addresses_id_seq; Type: SEQUENCE OWNED BY; Schema: bookstore; Owner: postgres
--

ALTER SEQUENCE bookstore.addresses_id_seq OWNED BY bookstore.addresses.id;


--
-- Name: books; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.books (
    product_id integer NOT NULL,
    author character varying(100),
    publisher character varying(100),
    isbn character varying(20),
    genre character varying(100),
    page_count integer,
    publication_date date,
    dimensions character varying(50),
    weight_grams integer,
    additional_info jsonb
);


ALTER TABLE bookstore.books OWNER TO postgres;

--
-- Name: cart_items; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.cart_items (
    user_id integer NOT NULL,
    product_id integer NOT NULL,
    quantity integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE bookstore.cart_items OWNER TO postgres;

--
-- Name: categories; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.categories (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    description text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    type character varying(50) DEFAULT 'book'::character varying,
    parent_id integer
);


ALTER TABLE bookstore.categories OWNER TO postgres;

--
-- Name: categories_id_seq; Type: SEQUENCE; Schema: bookstore; Owner: postgres
--

CREATE SEQUENCE bookstore.categories_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE bookstore.categories_id_seq OWNER TO postgres;

--
-- Name: categories_id_seq; Type: SEQUENCE OWNED BY; Schema: bookstore; Owner: postgres
--

ALTER SEQUENCE bookstore.categories_id_seq OWNED BY bookstore.categories.id;


--
-- Name: flyway_schema_history; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE bookstore.flyway_schema_history OWNER TO postgres;

--
-- Name: notebooks; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.notebooks (
    product_id integer NOT NULL,
    brand character varying(100),
    size character varying(50),
    page_count integer,
    paper_type character varying(100)
);


ALTER TABLE bookstore.notebooks OWNER TO postgres;

--
-- Name: order_items; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.order_items (
    order_id integer NOT NULL,
    product_id integer NOT NULL,
    quantity integer NOT NULL,
    price numeric(10,2) NOT NULL
);


ALTER TABLE bookstore.order_items OWNER TO postgres;

--
-- Name: orders; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.orders (
    id integer NOT NULL,
    user_id integer,
    address_id integer,
    total numeric(12,2) NOT NULL,
    status character varying(50) DEFAULT 'PENDING'::character varying,
    note text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE bookstore.orders OWNER TO postgres;

--
-- Name: orders_id_seq; Type: SEQUENCE; Schema: bookstore; Owner: postgres
--

CREATE SEQUENCE bookstore.orders_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE bookstore.orders_id_seq OWNER TO postgres;

--
-- Name: orders_id_seq; Type: SEQUENCE OWNED BY; Schema: bookstore; Owner: postgres
--

ALTER SEQUENCE bookstore.orders_id_seq OWNED BY bookstore.orders.id;


--
-- Name: payments; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.payments (
    id integer NOT NULL,
    order_id integer,
    payment_method character varying(50),
    payment_type character varying(50),
    payment_status character varying(50),
    transaction_id character varying(100),
    amount numeric(12,2),
    paid_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE bookstore.payments OWNER TO postgres;

--
-- Name: payments_id_seq; Type: SEQUENCE; Schema: bookstore; Owner: postgres
--

CREATE SEQUENCE bookstore.payments_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE bookstore.payments_id_seq OWNER TO postgres;

--
-- Name: payments_id_seq; Type: SEQUENCE OWNED BY; Schema: bookstore; Owner: postgres
--

ALTER SEQUENCE bookstore.payments_id_seq OWNED BY bookstore.payments.id;


--
-- Name: pens; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.pens (
    product_id integer NOT NULL,
    brand character varying(100),
    color character varying(50),
    pen_type character varying(50),
    material character varying(100)
);


ALTER TABLE bookstore.pens OWNER TO postgres;

--
-- Name: product_categories; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.product_categories (
    product_id integer NOT NULL,
    category_id integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE bookstore.product_categories OWNER TO postgres;

--
-- Name: product_images; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.product_images (
    id integer NOT NULL,
    product_id integer,
    image_url character varying(255) NOT NULL,
    is_thumbnail boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE bookstore.product_images OWNER TO postgres;

--
-- Name: product_images_id_seq; Type: SEQUENCE; Schema: bookstore; Owner: postgres
--

CREATE SEQUENCE bookstore.product_images_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE bookstore.product_images_id_seq OWNER TO postgres;

--
-- Name: product_images_id_seq; Type: SEQUENCE OWNED BY; Schema: bookstore; Owner: postgres
--

ALTER SEQUENCE bookstore.product_images_id_seq OWNED BY bookstore.product_images.id;


--
-- Name: products; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.products (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    price numeric(10,2) NOT NULL,
    category_id integer,
    description text,
    stock integer DEFAULT 0,
    type character varying(50) NOT NULL,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    active boolean DEFAULT true
);


ALTER TABLE bookstore.products OWNER TO postgres;

--
-- Name: products_id_seq; Type: SEQUENCE; Schema: bookstore; Owner: postgres
--

CREATE SEQUENCE bookstore.products_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE bookstore.products_id_seq OWNER TO postgres;

--
-- Name: products_id_seq; Type: SEQUENCE OWNED BY; Schema: bookstore; Owner: postgres
--

ALTER SEQUENCE bookstore.products_id_seq OWNED BY bookstore.products.id;


--
-- Name: roles; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.roles (
    id integer NOT NULL,
    name character varying(50) NOT NULL
);


ALTER TABLE bookstore.roles OWNER TO postgres;

--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: bookstore; Owner: postgres
--

CREATE SEQUENCE bookstore.roles_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE bookstore.roles_id_seq OWNER TO postgres;

--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: bookstore; Owner: postgres
--

ALTER SEQUENCE bookstore.roles_id_seq OWNED BY bookstore.roles.id;


--
-- Name: rulers; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.rulers (
    product_id integer NOT NULL,
    brand character varying(100),
    length_cm integer,
    material character varying(100)
);


ALTER TABLE bookstore.rulers OWNER TO postgres;

--
-- Name: user_roles; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.user_roles (
    user_id integer NOT NULL,
    role_id integer NOT NULL
);


ALTER TABLE bookstore.user_roles OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: bookstore; Owner: postgres
--

CREATE TABLE bookstore.users (
    id integer NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    email character varying(100) NOT NULL,
    full_name character varying(100),
    phone character varying(20),
    enabled boolean DEFAULT true,
    last_login timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    avatar character varying(255)
);


ALTER TABLE bookstore.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: bookstore; Owner: postgres
--

CREATE SEQUENCE bookstore.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE bookstore.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: bookstore; Owner: postgres
--

ALTER SEQUENCE bookstore.users_id_seq OWNED BY bookstore.users.id;


--
-- Name: addresses id; Type: DEFAULT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.addresses ALTER COLUMN id SET DEFAULT nextval('bookstore.addresses_id_seq'::regclass);


--
-- Name: categories id; Type: DEFAULT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.categories ALTER COLUMN id SET DEFAULT nextval('bookstore.categories_id_seq'::regclass);


--
-- Name: orders id; Type: DEFAULT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.orders ALTER COLUMN id SET DEFAULT nextval('bookstore.orders_id_seq'::regclass);


--
-- Name: payments id; Type: DEFAULT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.payments ALTER COLUMN id SET DEFAULT nextval('bookstore.payments_id_seq'::regclass);


--
-- Name: product_images id; Type: DEFAULT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.product_images ALTER COLUMN id SET DEFAULT nextval('bookstore.product_images_id_seq'::regclass);


--
-- Name: products id; Type: DEFAULT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.products ALTER COLUMN id SET DEFAULT nextval('bookstore.products_id_seq'::regclass);


--
-- Name: roles id; Type: DEFAULT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.roles ALTER COLUMN id SET DEFAULT nextval('bookstore.roles_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.users ALTER COLUMN id SET DEFAULT nextval('bookstore.users_id_seq'::regclass);


--
-- Name: addresses addresses_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.addresses
    ADD CONSTRAINT addresses_pkey PRIMARY KEY (id);


--
-- Name: books books_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.books
    ADD CONSTRAINT books_pkey PRIMARY KEY (product_id);


--
-- Name: cart_items cart_items_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.cart_items
    ADD CONSTRAINT cart_items_pkey PRIMARY KEY (user_id, product_id);


--
-- Name: categories categories_name_key; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.categories
    ADD CONSTRAINT categories_name_key UNIQUE (name);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: notebooks notebooks_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.notebooks
    ADD CONSTRAINT notebooks_pkey PRIMARY KEY (product_id);


--
-- Name: order_items order_items_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.order_items
    ADD CONSTRAINT order_items_pkey PRIMARY KEY (order_id, product_id);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: payments payments_order_id_key; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.payments
    ADD CONSTRAINT payments_order_id_key UNIQUE (order_id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);


--
-- Name: pens pens_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.pens
    ADD CONSTRAINT pens_pkey PRIMARY KEY (product_id);


--
-- Name: product_categories product_categories_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.product_categories
    ADD CONSTRAINT product_categories_pkey PRIMARY KEY (product_id, category_id);


--
-- Name: product_images product_images_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.product_images
    ADD CONSTRAINT product_images_pkey PRIMARY KEY (id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: roles roles_name_key; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.roles
    ADD CONSTRAINT roles_name_key UNIQUE (name);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: rulers rulers_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.rulers
    ADD CONSTRAINT rulers_pkey PRIMARY KEY (product_id);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: bookstore; Owner: postgres
--

CREATE INDEX flyway_schema_history_s_idx ON bookstore.flyway_schema_history USING btree (success);


--
-- Name: idx_books_genre; Type: INDEX; Schema: bookstore; Owner: postgres
--

CREATE INDEX idx_books_genre ON bookstore.books USING btree (genre);


--
-- Name: idx_books_publication_date; Type: INDEX; Schema: bookstore; Owner: postgres
--

CREATE INDEX idx_books_publication_date ON bookstore.books USING btree (publication_date);


--
-- Name: idx_categories_type; Type: INDEX; Schema: bookstore; Owner: postgres
--

CREATE INDEX idx_categories_type ON bookstore.categories USING btree (type);


--
-- Name: idx_product_categories_category; Type: INDEX; Schema: bookstore; Owner: postgres
--

CREATE INDEX idx_product_categories_category ON bookstore.product_categories USING btree (category_id);


--
-- Name: idx_product_categories_product; Type: INDEX; Schema: bookstore; Owner: postgres
--

CREATE INDEX idx_product_categories_product ON bookstore.product_categories USING btree (product_id);


--
-- Name: books update_books_updated_at; Type: TRIGGER; Schema: bookstore; Owner: postgres
--

CREATE TRIGGER update_books_updated_at BEFORE UPDATE ON bookstore.books FOR EACH ROW EXECUTE FUNCTION bookstore.update_updated_at_column();


--
-- Name: products update_products_updated_at_trigger; Type: TRIGGER; Schema: bookstore; Owner: postgres
--

CREATE TRIGGER update_products_updated_at_trigger BEFORE UPDATE ON bookstore.products FOR EACH ROW EXECUTE FUNCTION bookstore.update_products_updated_at();


--
-- Name: addresses addresses_user_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.addresses
    ADD CONSTRAINT addresses_user_id_fkey FOREIGN KEY (user_id) REFERENCES bookstore.users(id) ON DELETE CASCADE;


--
-- Name: books books_product_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.books
    ADD CONSTRAINT books_product_id_fkey FOREIGN KEY (product_id) REFERENCES bookstore.products(id) ON DELETE CASCADE;


--
-- Name: cart_items cart_items_product_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.cart_items
    ADD CONSTRAINT cart_items_product_id_fkey FOREIGN KEY (product_id) REFERENCES bookstore.products(id);


--
-- Name: cart_items cart_items_user_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.cart_items
    ADD CONSTRAINT cart_items_user_id_fkey FOREIGN KEY (user_id) REFERENCES bookstore.users(id) ON DELETE CASCADE;


--
-- Name: categories categories_parent_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.categories
    ADD CONSTRAINT categories_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES bookstore.categories(id) ON DELETE CASCADE;


--
-- Name: notebooks notebooks_product_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.notebooks
    ADD CONSTRAINT notebooks_product_id_fkey FOREIGN KEY (product_id) REFERENCES bookstore.products(id) ON DELETE CASCADE;


--
-- Name: order_items order_items_order_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.order_items
    ADD CONSTRAINT order_items_order_id_fkey FOREIGN KEY (order_id) REFERENCES bookstore.orders(id) ON DELETE CASCADE;


--
-- Name: order_items order_items_product_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.order_items
    ADD CONSTRAINT order_items_product_id_fkey FOREIGN KEY (product_id) REFERENCES bookstore.products(id);


--
-- Name: orders orders_address_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.orders
    ADD CONSTRAINT orders_address_id_fkey FOREIGN KEY (address_id) REFERENCES bookstore.addresses(id);


--
-- Name: orders orders_user_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.orders
    ADD CONSTRAINT orders_user_id_fkey FOREIGN KEY (user_id) REFERENCES bookstore.users(id) ON DELETE CASCADE;


--
-- Name: payments payments_order_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.payments
    ADD CONSTRAINT payments_order_id_fkey FOREIGN KEY (order_id) REFERENCES bookstore.orders(id) ON DELETE CASCADE;


--
-- Name: pens pens_product_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.pens
    ADD CONSTRAINT pens_product_id_fkey FOREIGN KEY (product_id) REFERENCES bookstore.products(id) ON DELETE CASCADE;


--
-- Name: product_categories product_categories_category_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.product_categories
    ADD CONSTRAINT product_categories_category_id_fkey FOREIGN KEY (category_id) REFERENCES bookstore.categories(id) ON DELETE CASCADE;


--
-- Name: product_categories product_categories_product_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.product_categories
    ADD CONSTRAINT product_categories_product_id_fkey FOREIGN KEY (product_id) REFERENCES bookstore.products(id) ON DELETE CASCADE;


--
-- Name: product_images product_images_product_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.product_images
    ADD CONSTRAINT product_images_product_id_fkey FOREIGN KEY (product_id) REFERENCES bookstore.products(id) ON DELETE CASCADE;


--
-- Name: products products_category_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.products
    ADD CONSTRAINT products_category_id_fkey FOREIGN KEY (category_id) REFERENCES bookstore.categories(id);


--
-- Name: rulers rulers_product_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.rulers
    ADD CONSTRAINT rulers_product_id_fkey FOREIGN KEY (product_id) REFERENCES bookstore.products(id) ON DELETE CASCADE;


--
-- Name: user_roles user_roles_role_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.user_roles
    ADD CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES bookstore.roles(id) ON DELETE CASCADE;


--
-- Name: user_roles user_roles_user_id_fkey; Type: FK CONSTRAINT; Schema: bookstore; Owner: postgres
--

ALTER TABLE ONLY bookstore.user_roles
    ADD CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES bookstore.users(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

