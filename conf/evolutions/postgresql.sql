-- FIRST INIT

-- create database seyhan;

-- \c seyhan;

BEGIN;

create table admin_document (
  id                        serial not null,
  module                    varchar(10) not null,
  header                    varchar(20),
  _right                    varchar(50) not null,
  name                      varchar(20) not null,
  page_rows                 numeric(3) default 66,
  report_title_rows         numeric(3) default 0,
  page_title_rows           numeric(3) default 3,
  detail_rows               numeric(3) default 1,
  page_footer_rows          numeric(3) default 3,
  report_footer_rows        numeric(3) default 0,
  report_title_labels       boolean default true,
  page_title_labels         boolean default true,
  detail_labels             boolean default true,
  page_footer_labels        boolean default true,
  report_footer_labels      boolean default true,
  left_margin               numeric(3) default 0,
  top_margin                numeric(3) default 0,
  bottom_margin             numeric(3) default 0,
  is_single_page            boolean default false,
  has_paging                boolean default true,
  column_title_type         varchar(7),
  carrying_over_name        varchar(50),
  description               varchar(30),
  template_rows             text,
  is_active                 boolean default true,
  version                   integer default 0,
  primary key (id)
);
create unique index admin_document_ix1 on admin_document (name);
create sequence admin_document_seq;

create table admin_document_field (
  id                        serial not null,
  module                    varchar(10),
  band                      varchar(12),
  _type                     varchar(20),
  name                      varchar(100),
  nick_name                 varchar(100),
  hidden_field              varchar(100),
  _label                    varchar(70),
  original_label            varchar(70),
  label_width               numeric(3),
  label_align               varchar(5),
  _width                    numeric(3),
  _row                      numeric(3),
  _column                   numeric(3),
  _format                   varchar(30),
  prefix                    varchar(5),
  suffix                    varchar(5),
  _value                    varchar(70),
  msg_prefix                varchar(30),
  defauld                   varchar(50),
  is_db_field               boolean default true,
  table_type                varchar(10),
  report_title_doc_id       integer,
  page_title_doc_id         integer,
  detail_doc_id             integer,
  page_footer_doc_id        integer,
  report_footer_doc_id      integer,
  primary key (id)
);
create sequence admin_document_field_seq;

create table admin_document_target (
  id                        serial not null,
  name                      varchar(30) not null,
  is_local                  boolean default true,
  target_type               varchar(10),
  view_type                 varchar(9),
  path                      varchar(150),
  is_compressed             boolean default true,
  description               varchar(30),
  is_active                 boolean default true,
  version                   integer default 0,
  primary key (id)
);
create unique index admin_document_target_ix1 on admin_document_target (name);
create sequence admin_document_target_seq;

create table admin_extra_fields (
  id                        serial not null,
  idno                      integer not null,
  distinction               varchar(15) not null,
  name                      varchar(12) not null,
  is_required               boolean default false,
  is_active                 boolean default true,
  primary key (id)
);
create unique index admin_extra_fields_ix1 on admin_extra_fields (distinction, name);
create sequence admin_extra_fields_seq;

create table admin_setting (
  id                        serial not null,
  code                      varchar(10) not null,
  description               varchar(30),
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  json_data                 text,
  version                   integer default 0,
  primary key (id)
);
create unique index admin_setting_ix1 on admin_setting (code);
create sequence admin_setting_seq;

create table admin_user (
  id                        serial not null,
  username                  varchar(20) not null,
  title                     varchar(30),
  email                     varchar(100),
  auth_token                varchar(32),
  password_hash             varchar(60),
  is_admin                  boolean default false,
  is_active                 boolean default true,
  profile                   varchar(20),
  workspace                 integer,
  user_group_id             integer,
  version                   integer default 0,
  primary key (id)
);
create unique index admin_user_ix1 on admin_user (username);
create sequence admin_user_seq;

create table admin_user_audit (
  id                        serial not null,
  username                  varchar(20),
  _date                     timestamp,
  _right                    varchar(30),
  ip                        varchar(45),
  description               varchar(255),
  log_level                 varchar(7),
  workspace                 varchar(30),
  primary key (id)
);
create index admin_user_audit_ix1 on admin_user_audit (workspace, _date, username);
create sequence admin_user_audit_seq;

create table admin_user_given_role (
  id                        serial not null,
  user_group_id             integer,
  workspace_id              integer,
  user_role_id              integer,
  primary key (id)
);
create sequence admin_user_given_role_seq;

create table admin_user_group (
  id                        serial not null,
  name                      varchar(30) not null,
  description               varchar(50),
  editing_timeout           smallint default 0,
  editing_limit             varchar(10),
  has_edit_dif_date         boolean default false,
  version                   integer default 0,
  primary key (id)
);
create unique index admin_user_group_ix1 on admin_user_group (name);
create sequence admin_user_group_seq;

create table admin_user_right (
  id                        serial not null,
  name                      varchar(50) not null,
  right_level               varchar(7) not null,
  is_crud                   boolean default false,
  user_role_id              integer,
  primary key (id)
);
create index admin_user_right_ix1 on admin_user_right (name);
create sequence admin_user_right_seq;

create table admin_user_role (
  id                        serial not null,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  version                   integer default 0,
  primary key (id)
);
create unique index admin_user_role_ix1 on admin_user_role (name);
create sequence admin_user_role_seq;

create table admin_workspace (
  id                        serial not null,
  name                      varchar(30) not null,
  description               varchar(50),
  start_date                date,
  end_date                  date,
  has_date_restriction      boolean default false,
  is_active                 boolean default true,
  version                   integer default 0,
  primary key (id)
);
create unique index admin_workspace_ix1 on admin_workspace (name);
create sequence admin_workspace_seq;

create table bank (
  id                        serial not null,
  account_no                varchar(15) not null,
  name                      varchar(50) not null,
  branch                    varchar(30),
  city                      varchar(20),
  iban                      varchar(26),
  exc_code                  varchar(3),
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index bank_ix1 on bank (workspace, name);
create sequence bank_seq;

create table bank_expense (
  id                        serial not null,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index bank_expense_ix1 on bank_expense (workspace, name);
create sequence bank_expense_seq;

create table bank_trans (
  id                        serial not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  trans_no                  varchar(20),
  trans_type                varchar(6) not null,
  amount                    real default 0 not null,
  debt                      real default 0 not null,
  credit                    real default 0 not null,
  description               varchar(100),
  trans_year                numeric(4),
  trans_month               varchar(7),
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  expense_amount            real default 0,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  bank_id                   integer not null,
  expense_id                integer,
  ref_module                varchar(10),
  ref_id                    integer,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index bank_trans_ix1 on bank_trans (workspace, _right, trans_date);
create sequence bank_trans_seq;

create table bank_trans_source (
  id                        serial not null,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index bank_trans_source_ix1 on bank_trans_source (workspace, name);
create sequence bank_trans_source_seq;

create table chqbll_payroll (
  id                        serial not null,
  sort                      varchar(6) not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  trans_no                  varchar(20),
  trans_type                varchar(6) not null,
  total                     real default 0 not null,
  row_count                 integer default 0 not null,
  adat                      integer default 0 not null,
  avarage_date              date not null,
  description               varchar(100),
  trans_year                numeric(4),
  trans_month               varchar(7),
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  contact_id                integer,
  ref_module                varchar(10),
  ref_id                    integer,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index chqbll_payroll_ix1 on chqbll_payroll (workspace, sort, _right, trans_date);
create sequence chqbll_payroll_seq;

create table chqbll_payroll_detail (
  id                        serial not null,
  sort                      varchar(6) not null,
  is_customer               boolean default true,
  portfolio_no              integer not null,
  row_no                    integer not null,
  serial_no                 varchar(25),
  due_date                  date not null,
  amount                    real default 0 not null,
  description               varchar(100),
  due_year                  numeric(4),
  due_month                 varchar(7),
  owner                     varchar(70),
  payment_place             varchar(30),
  bank_account_no           varchar(15),
  bank_name                 varchar(50),
  bank_branch               varchar(30),
  correspondent_branch      varchar(30),
  contact_name              varchar(100),
  last_step                 varchar(15) not null,
  last_contact_name         varchar(100),
  surety                    varchar(100),
  surety_address            varchar(100),
  surety_phone1             varchar(15),
  surety_phone2             varchar(15),
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  total_paid                real default 0,
  cbtype_id                 integer,
  trans_id                  integer,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  contact_id                integer,
  bank_id                   integer,
  workspace                 integer not null,
  primary key (id)
);
create index chqbll_payroll_detail_ix1 on chqbll_payroll_detail (workspace, is_customer, sort, due_date, last_step);
create sequence chqbll_payroll_detail_seq;

create table chqbll_detail_history (
  id                        serial not null,
  sort                      varchar(6) not null,
  step_date                 date not null,
  step                      varchar(15) not null,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  detail_id                 integer,
  contact_id                integer,
  bank_id                   integer,
  safe_id                   integer,
  primary key (id)
);
create index chqbll_detail_history_ix1 on chqbll_detail_history (sort, step_date);
create sequence chqbll_detail_history_seq;

create table chqbll_detail_partial (
  id                        serial not null,
  sort                      varchar(6) not null,
  is_customer               boolean default true,
  trans_date                date not null,
  amount                    real default 0 not null,
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  description               varchar(100),
  insert_by                 varchar(20),
  insert_at                 timestamp,
  detail_id                 integer,
  safe_id                   integer,
  trans_id                  integer,
  primary key (id)
);
create index chqbll_detail_partial_ix1 on chqbll_detail_partial (sort, is_customer, trans_date);
create sequence chqbll_detail_partial_seq;

create table chqbll_payroll_source (
  id                        serial not null,
  sort                      varchar(6) not null,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index chqbll_payroll_source_ix1 on chqbll_payroll_source (workspace, name);
create sequence chqbll_payroll_source_seq;

create table chqbll_trans (
  id                        serial not null,
  sort                      varchar(6) not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  from_step                 varchar(15) not null,
  to_step                   varchar(15) not null,
  trans_date                date not null,
  trans_no                  varchar(20),
  trans_type                varchar(6) not null,
  total                     real default 0 not null,
  row_count                 integer default 0 not null,
  adat                      integer default 0 not null,
  avarage_date              date not null,
  description               varchar(100),
  trans_year                numeric(4),
  trans_month               varchar(7),
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  contact_id                integer,
  bank_id                   integer,
  safe_id                   integer,
  ref_module                varchar(10),
  ref_id                    integer,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index chqbll_trans_ix1 on chqbll_trans (workspace, sort, _right, trans_date);
create sequence chqbll_trans_seq;

create table chqbll_trans_detail (
  id                        serial not null,
  trans_id                  integer,
  detail_id                 integer,
  primary key (id)
);
create sequence chqbll_trans_detail_seq;

create table chqbll_type (
  id                        serial not null,
  sort                      varchar(6) not null,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index chqbll_type_ix1 on chqbll_type (workspace, name);
create sequence chqbll_type_seq;

create table contact (
  id                        serial not null,
  code                      varchar(30) not null,
  name                      varchar(100) not null,
  tax_office                varchar(20),
  tax_number                varchar(15),
  tc_kimlik                 varchar(11),
  relevant                  varchar(30),
  phone                     varchar(15),
  fax                       varchar(15),
  mobile_phone              varchar(15),
  address1                  varchar(100),
  address2                  varchar(100),
  city                      varchar(20),
  country                   varchar(20),
  email                     varchar(100),
  website                   varchar(100),
  status                    varchar(12),
  exc_code                  varchar(3),
  note                      text,
  is_active                 boolean default true,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  seller_id                 integer,
  category_id               integer,
  price_list_id             integer,
  extra_field0_id           integer,
  extra_field1_id           integer,
  extra_field2_id           integer,
  extra_field3_id           integer,
  extra_field4_id           integer,
  extra_field5_id           integer,
  extra_field6_id           integer,
  extra_field7_id           integer,
  extra_field8_id           integer,
  extra_field9_id           integer,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index contact_ix1 on contact (workspace, name);
create index contact_ix2 on contact (workspace, code);
create sequence contact_seq;

create table contact_category (
  id                        serial not null,
  name                      varchar(30) not null,
  working_dir               varchar(6),
  debt_limit                real default 0,
  credit_limit              real default 0,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index contact_category_ix1 on contact_category (workspace, name);
create sequence contact_category_seq;

create table contact_extra_fields (
  id                        serial not null,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  extra_fields_id           integer,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index contact_extra_fields_ix1 on contact_extra_fields (workspace, extra_fields_id, name);
create sequence contact_extra_fields_seq;

create table contact_trans (
  id                        serial not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  maturity                  date,
  trans_no                  varchar(20),
  trans_type                varchar(6) not null,
  amount                    real default 0 not null,
  debt                      real default 0 not null,
  credit                    real default 0 not null,
  description               varchar(100),
  trans_year                numeric(4),
  trans_month               varchar(7),
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  contact_id                integer not null,
  ref_module                varchar(10),
  ref_id                    integer,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index contact_trans_ix1 on contact_trans (workspace, _right, trans_date);
create sequence contact_trans_seq;

create table contact_trans_source (
  id                        serial not null,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index contact_trans_source_ix1 on contact_trans_source (workspace, name);
create sequence contact_trans_source_seq;

create table global_currency (
  id                        serial not null,
  code                      varchar(3) not null,
  name                      varchar(25) not null,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default false,
  version                   integer default 0,
  primary key (id)
);
create unique index global_currency_ix1 on global_currency (code);
create sequence global_currency_seq;

create table global_currency_rate (
  id                        serial not null,
  _date                     date not null,
  source                    varchar(100),
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  version                   integer default 0,
  primary key (id)
);
create unique index global_currency_rate_ix1 on global_currency_rate (_date);
create sequence global_currency_rate_seq;

create table global_currency_rate_detail (
  id                        serial not null,
  _date                     date not null,
  code                      varchar(3) not null,
  name                      varchar(25) not null,
  buying                    real default 1,
  selling                   real default 1,
  currency_rate_id          integer,
  primary key (id)
);
create unique index global_currency_rate_detail_ix1 on global_currency_rate_detail (_date, code);
create sequence global_currency_rate_detail_seq;

create table global_private_code (
  id                        serial not null,
  par1id                    integer,
  par2id                    integer,
  par3id                    integer,
  par4id                    integer,
  par5id                    integer,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index global_private_code_ix1 on global_private_code (workspace, name);
create sequence global_private_code_seq;

create table global_profile (
  id                        serial not null,
  name                      varchar(20) not null,
  description               varchar(30),
  is_active                 boolean default true,
  json_data                 text,
  version                   integer default 0,
  primary key (id)
);
create unique index global_profile_ix1 on global_profile (name);
create sequence global_profile_seq;

create table global_trans_point (
  id                        serial not null,
  par1id                    integer,
  par2id                    integer,
  par3id                    integer,
  par4id                    integer,
  par5id                    integer,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index global_trans_point_ix1 on global_trans_point (workspace, name);
create sequence global_trans_point_seq;

create table invoice_trans (
  id                        serial not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  is_cash                   boolean default true,
  is_completed              boolean default false,
  trans_date                date not null,
  real_date                 timestamp,
  delivery_date             timestamp,
  trans_no                  varchar(20),
  is_tax_include            boolean default true,
  rounding_digits           numeric(1),
  total                     real default 0,
  discount_total            real default 0,
  subtotal                  real default 0,
  rounding_discount         real default 0,
  total_discount_rate       real default 0,
  tax_total                 real default 0,
  net_total                 real default 0,
  plus_factor_total         real default 0,
  minus_factor_total        real default 0,
  withholding_rate          real default 0,
  withholding_before        real default 0,
  withholding_amount        real default 0,
  withholding_after         real default 0,
  description               varchar(100),
  trans_year                numeric(4),
  trans_month               varchar(7),
  contact_id                integer,
  contact_name              varchar(100),
  contact_tax_office        varchar(20),
  contact_tax_number        varchar(15),
  contact_address1          varchar(100),
  contact_address2          varchar(100),
  consigner                 varchar(50),
  recepient                 varchar(50),
  trans_type                varchar(6) not null,
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  contact_trans_id          integer,
  seller_id                 integer,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  depot_id                  integer,
  ref_module                varchar(10),
  ref_id                    integer,
  status_id                 integer,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index invoice_trans_ix1 on invoice_trans (workspace, _right, trans_date);
create sequence invoice_trans_seq;

create table invoice_trans_detail (
  id                        serial not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  delivery_date             date,
  trans_type                varchar(6) not null,
  row_no                    integer,
  stock_id                  integer,
  name                      varchar(100),
  quantity                  real default 1,
  unit                      varchar(6),
  unit_ratio                real default 1,
  base_price                real default 0,
  price                     real default 0,
  tax_rate                  real default 0,
  tax_rate2                 real default 0,
  tax_rate3                 real default 0,
  discount_rate1            real default 0,
  discount_rate2            real default 0,
  discount_rate3            real default 0,
  amount                    real default 0,
  tax_amount                real default 0,
  discount_amount           real default 0,
  total                     real default 0,
  description               varchar(100),
  trans_year                numeric(4),
  trans_month               varchar(7),
  unit1                     varchar(6),
  unit2                     varchar(6),
  unit3                     varchar(6),
  unit2ratio                real default 0,
  unit3ratio                real default 0,
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  plus_factor_amount        real default 0,
  minus_factor_amount       real default 0,
  serial_no                 varchar(100),
  input                     real default 0,
  output                    real default 0,
  in_total                  real default 0,
  out_total                 real default 0,
  is_return                 boolean default false,
  ret_input                 real default 0,
  ret_output                real default 0,
  ret_in_total              real default 0,
  ret_out_total             real default 0,
  net_input                 real default 0,
  net_output                real default 0,
  net_in_total              real default 0,
  net_out_total             real default 0,
  has_cost_effect           boolean default true,
  trans_id                  integer,
  depot_id                  integer,
  contact_id                integer,
  seller_id                 integer,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  parent_id                 integer,
  parent_right              varchar(30),
  status_id                 integer,
  workspace                 integer not null,
  primary key (id)
);
create index invoice_trans_detail_ix1 on invoice_trans_detail (workspace, trans_date);
create sequence invoice_trans_detail_seq;

create table invoice_trans_factor (
  id                        serial not null,
  effect                    real default 0,
  amount                    real default 0,
  trans_id                  integer,
  factor_id                 integer,
  primary key (id)
);
create sequence invoice_trans_factor_seq;

create table invoice_trans_relation (
  id                        serial not null,
  rel_id                    integer not null,
  rel_right                 varchar(30) not null,
  rel_receipt_no            integer not null,
  trans_id                  integer,
  primary key (id)
);
create sequence invoice_trans_relation_seq;

create table invoice_trans_source (
  id                        serial not null,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  has_cost_effect           boolean default true,
  has_stock_effect          boolean default true,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index invoice_trans_source_ix1 on invoice_trans_source (workspace, name);
create sequence invoice_trans_source_seq;

create table invoice_trans_status (
  id                        serial not null,
  parent_id                 integer,
  name                      varchar(30) not null,
  ordering                  integer default 0,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index invoice_trans_status_ix1 on invoice_trans_status (name);
create sequence invoice_trans_status_seq;

create table invoice_trans_status_history (
  id                        serial not null,
  trans_time                timestamp,
  trans_id                  integer not null,
  status_id                 integer not null,
  username                  varchar(20),
  description               varchar(150),
  primary key (id)
);
create sequence invoice_trans_status_history_seq;

create table invoice_trans_tax (
  id                        serial not null,
  tax_rate                  real default 0,
  basis                     real default 0,
  amount                    real default 0,
  trans_id                  integer,
  primary key (id)
);
create sequence invoice_trans_tax_seq;

create table invoice_trans_currency (
  id                        serial not null,
  currency                  varchar(3) default '',
  amount                    real default 0,
  trans_id                  integer,
  primary key (id)
);
create sequence invoice_trans_currency_seq;

create table order_trans (
  id                        serial not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  is_completed              boolean default false,
  trans_date                date not null,
  real_date                 timestamp,
  delivery_date             timestamp,
  trans_no                  varchar(20),
  is_tax_include            boolean default true,
  rounding_digits           numeric(1),
  total                     real default 0,
  discount_total            real default 0,
  subtotal                  real default 0,
  rounding_discount         real default 0,
  total_discount_rate       real default 0,
  tax_total                 real default 0,
  net_total                 real default 0,
  plus_factor_total         real default 0,
  minus_factor_total        real default 0,
  description               varchar(100),
  trans_year                numeric(4),
  trans_month               varchar(7),
  contact_id                integer,
  contact_name              varchar(100),
  contact_tax_office        varchar(20),
  contact_tax_number        varchar(15),
  contact_address1          varchar(100),
  contact_address2          varchar(100),
  consigner                 varchar(50),
  recepient                 varchar(50),
  trans_type                varchar(6) not null,
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  is_transfer               boolean default false,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  contact_trans_id          integer,
  seller_id                 integer,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  depot_id                  integer,
  waybill_id                integer,
  invoice_id                integer,
  ref_module                varchar(10),
  ref_id                    integer,
  status_id                 integer,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index order_trans_ix1 on order_trans (workspace, _right, trans_date);
create sequence order_trans_seq;

create table order_trans_detail (
  id                        serial not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  delivery_date             date,
  trans_type                varchar(6) not null,
  row_no                    integer,
  stock_id                  integer,
  name                      varchar(100),
  quantity                  real default 1,
  unit                      varchar(6),
  unit_ratio                real default 1,
  base_price                real default 0,
  price                     real default 0,
  tax_rate                  real default 0,
  discount_rate1            real default 0,
  discount_rate2            real default 0,
  discount_rate3            real default 0,
  amount                    real default 0,
  tax_amount                real default 0,
  discount_amount           real default 0,
  total                     real default 0,
  description               varchar(100),
  trans_year                numeric(4),
  trans_month               varchar(7),
  unit1                     varchar(6),
  unit2                     varchar(6),
  unit3                     varchar(6),
  unit2ratio                real default 0,
  unit3ratio                real default 0,
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  plus_factor_amount        real default 0,
  minus_factor_amount       real default 0,
  input                     real default 0,
  output                    real default 0,
  in_total                  real default 0,
  out_total                 real default 0,
  net_input                 real default 0,
  net_output                real default 0,
  net_in_total              real default 0,
  net_out_total             real default 0,
  completed                 real default 0,
  cancelled                 real default 0,
  is_transfer               boolean default false,
  trans_id                  integer,
  depot_id                  integer,
  contact_id                integer,
  seller_id                 integer,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  status_id                 integer,
  workspace                 integer not null,
  primary key (id)
);
create index order_trans_detail_ix1 on order_trans_detail (workspace, trans_date);
create sequence order_trans_detail_seq;

create table order_trans_factor (
  id                        serial not null,
  effect                    real default 0,
  amount                    real default 0,
  trans_id                  integer,
  factor_id                 integer,
  primary key (id)
);
create sequence order_trans_factor_seq;

create table order_trans_source (
  id                        serial not null,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  version                   integer default 0,
  primary key (id)
);
create index order_trans_source_ix1 on order_trans_source (workspace, name);
create sequence order_trans_source_seq;

create table order_trans_status (
  id                        serial not null,
  parent_id                 integer,
  name                      varchar(30) not null,
  ordering                  integer default 0,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index order_trans_status_ix1 on order_trans_status (name);
create sequence order_trans_status_seq;

create table order_trans_status_history (
  id                        serial not null,
  trans_time                timestamp,
  trans_id                  integer not null,
  status_id                 integer not null,
  username                  varchar(20),
  description               varchar(150),
  primary key (id)
);
create sequence order_trans_status_history_seq;

create table safe (
  id                        serial not null,
  name                      varchar(50) not null,
  exc_code                  varchar(3),
  responsible               varchar(30),
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index safe_ix1 on safe (workspace, name);
create sequence safe_seq;

create table safe_expense (
  id                        serial not null,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index safe_expense_ix1 on safe_expense (workspace, name);
create sequence safe_expense_seq;

create table safe_trans (
  id                        serial not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  trans_no                  varchar(20),
  trans_type                varchar(6) not null,
  amount                    real default 0 not null,
  debt                      real default 0 not null,
  credit                    real default 0 not null,
  description               varchar(100),
  trans_year                numeric(4),
  trans_month               varchar(7),
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  safe_id                   integer not null,
  expense_id                integer,
  ref_module                varchar(10),
  ref_id                    integer,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index safe_trans_ix1 on safe_trans (workspace, _right, trans_date);
create sequence safe_trans_seq;

create table safe_trans_source (
  id                        serial not null,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index safe_trans_source_ix1 on safe_trans_source (workspace, name);
create sequence safe_trans_source_seq;

create table sale_seller (
  id                        serial not null,
  name                      varchar(30) not null,
  prim_rate                 real not null,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index sale_seller_ix1 on sale_seller (workspace, name);
create sequence sale_seller_seq;

create table sale_campaign (
  id                        serial not null,
  name                      varchar(30) not null,
  start_date                date not null,
  end_date                  date not null,
  discount_rate1            real default 0,
  discount_rate2            real default 0,
  discount_rate3            real default 0,
  priority                  numeric(2) default 1,
  stock_category_id         integer,
  extra_field0_id           integer,
  extra_field1_id           integer,
  extra_field2_id           integer,
  extra_field3_id           integer,
  extra_field4_id           integer,
  extra_field5_id           integer,
  extra_field6_id           integer,
  extra_field7_id           integer,
  extra_field8_id           integer,
  extra_field9_id           integer,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index sale_campaign_ix1 on sale_campaign (workspace, name);
create sequence sale_campaign_seq;

create table stock (
  id                        serial not null,
  code                      varchar(30) not null,
  name                      varchar(100) not null,
  exc_code                  varchar(3),
  provider_code             varchar(30),
  unit1                     varchar(6),
  unit2                     varchar(6),
  unit3                     varchar(6),
  unit2ratio                real default 0,
  unit3ratio                real default 0,
  buy_price                 real default 0,
  sell_price                real default 0,
  buy_tax                   real default 0,
  sell_tax                  real default 0,
  tax_rate2                 real default 0,
  tax_rate3                 real default 0,
  prim_rate                 real default 0,
  max_limit                 real default 0,
  min_limit                 real default 0,
  note                      text,
  category_id               integer,
  extra_field0_id           integer,
  extra_field1_id           integer,
  extra_field2_id           integer,
  extra_field3_id           integer,
  extra_field4_id           integer,
  extra_field5_id           integer,
  extra_field6_id           integer,
  extra_field7_id           integer,
  extra_field8_id           integer,
  extra_field9_id           integer,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index stock_ix1 on stock (workspace, name);
create index stock_ix2 on stock (workspace, code);
create sequence stock_seq;

create table stock_barcode (
  id                        serial not null,
  barcode                   varchar(128) not null,
  prefix                    varchar(30),
  suffix                    varchar(30),
  unit_no                   smallint default 1,
  is_primary                boolean default false,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  stock_id                  integer,
  workspace                 integer not null,
  primary key (id)
);
create index stock_barcode_ix1 on stock_barcode (workspace, barcode);
create sequence stock_barcode_seq;

create table stock_category (
  id                        serial not null,
  par1id                    integer,
  par2id                    integer,
  par3id                    integer,
  par4id                    integer,
  par5id                    integer,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index stock_category_ix1 on stock_category (workspace, name);
create sequence stock_category_seq;

create table stock_costing (
  id                        serial not null,
  name                      varchar(30),
  properties                varchar(100) not null,
  exec_date                 timestamp,
  calc_date                 date not null,
  costing_type              varchar(8) not null,
  provider_code             varchar(30),
  trans_point_id            integer,
  category_id               integer,
  depot_id                  integer,
  stock_id                  integer,
  extra_field0_id           integer,
  extra_field1_id           integer,
  extra_field2_id           integer,
  extra_field3_id           integer,
  extra_field4_id           integer,
  extra_field5_id           integer,
  extra_field6_id           integer,
  extra_field7_id           integer,
  extra_field8_id           integer,
  extra_field9_id           integer,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index stock_costing_ix1 on stock_costing (workspace, name);
create sequence stock_costing_seq;

create table stock_costing_detail (
  id                        serial not null,
  sell_date                 date not null,
  sell_quantity             real default 0,
  sell_cost_price           real default 0,
  sell_cost_amount          real default 0,
  buy_cost_price            real default 0,
  buy_cost_amount           real default 0,
  profit_loss_amount        real default 0,
  trans_year                numeric(4),
  trans_month               varchar(7),
  costing_id                integer,
  stock_id                  integer,
  primary key (id)
);
create sequence stock_costing_detail_seq;

create table stock_costing_inventory (
  id                        serial not null,
  _date                     date,
  input                     real default 0,
  remain                    real default 0,
  price                     real default 0,
  amount                    real default 0,
  costing_id                integer,
  stock_id                  integer,
  depot_id                  integer,
  primary key (id)
);
create sequence stock_costing_inventory_seq;

create table stock_cost_factor (
  id                        serial not null,
  name                      varchar(30) not null,
  factor_type               varchar(8) not null,
  calc_type                 varchar(7) not null,
  effect_type               varchar(7) not null,
  effect                    real,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index stock_cost_factor_ix1 on stock_cost_factor (workspace, name);
create sequence stock_cost_factor_seq;

create table stock_depot (
  id                        serial not null,
  name                      varchar(50) not null,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index stock_depot_ix1 on stock_depot (workspace, name);
create sequence stock_depot_seq;

create table stock_extra_fields (
  id                        serial not null,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  extra_fields_id           integer,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index stock_extra_fields_ix1 on stock_extra_fields (workspace, extra_fields_id, name);
create sequence stock_extra_fields_seq;

create table stock_price_list (
  id                        serial not null,
  name                      varchar(30) not null,
  start_date                date,
  end_date                  date,
  is_sell_price             boolean default true,
  effect_type               varchar(7) not null,
  effect_direction          varchar(8) not null,
  effect                    real default 0,
  description               varchar(50),
  provider_code             varchar(30),
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  category_id               integer,
  extra_field0_id           integer,
  extra_field1_id           integer,
  extra_field2_id           integer,
  extra_field3_id           integer,
  extra_field4_id           integer,
  extra_field5_id           integer,
  extra_field6_id           integer,
  extra_field7_id           integer,
  extra_field8_id           integer,
  extra_field9_id           integer,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index stock_price_list_ix1 on stock_price_list (workspace, name);
create sequence stock_price_list_seq;

create table stock_price_update (
  id                        serial not null,
  name                      varchar(30) not null,
  exec_date                 timestamp,
  effect_type               varchar(7) not null,
  effect_direction          varchar(8) not null,
  effect                    real default 0,
  description               varchar(50),
  buy_price                 boolean default false,
  sell_price                boolean default false,
  provider_code             varchar(30),
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  category_id               integer,
  extra_field0_id           integer,
  extra_field1_id           integer,
  extra_field2_id           integer,
  extra_field3_id           integer,
  extra_field4_id           integer,
  extra_field5_id           integer,
  extra_field6_id           integer,
  extra_field7_id           integer,
  extra_field8_id           integer,
  extra_field9_id           integer,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index stock_price_update_ix1 on stock_price_update (workspace, name);
create sequence stock_price_update_seq;

create table stock_price_update_detail (
  id                        serial not null,
  price_update_id           integer,
  stock_id                  integer,
  buy_price                 real default 0,
  sell_price                real default 0,
  primary key (id)
);
create sequence stock_price_update_detail_seq;

create table stock_trans (
  id                        serial not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  is_completed              boolean default false,
  trans_date                date not null,
  real_date                 timestamp,
  delivery_date             timestamp,
  trans_no                  varchar(20),
  is_tax_include            boolean default true,
  rounding_digits           numeric(1),
  total                     real default 0,
  discount_total            real default 0,
  subtotal                  real default 0,
  rounding_discount         real default 0,
  total_discount_rate       real default 0,
  tax_total                 real default 0,
  net_total                 real default 0,
  plus_factor_total         real default 0,
  minus_factor_total        real default 0,
  amount                    real default 0,
  description               varchar(100),
  trans_year                numeric(4),
  trans_month               varchar(7),
  contact_id                integer,
  contact_name              varchar(100),
  contact_tax_office        varchar(20),
  contact_tax_number        varchar(15),
  contact_address1          varchar(100),
  contact_address2          varchar(100),
  consigner                 varchar(50),
  recepient                 varchar(50),
  trans_type                varchar(6) not null,
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  contact_trans_id          integer,
  seller_id                 integer,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  depot_id                  integer,
  ref_depot_id              integer,
  ref_module                varchar(10),
  ref_id                    integer,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index stock_trans_ix1 on stock_trans (workspace, _right, trans_date);
create sequence stock_trans_seq;

create table stock_trans_detail (
  id                        serial not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  delivery_date             date,
  trans_type                varchar(6) not null,
  row_no                    integer,
  stock_id                  integer,
  name                      varchar(100),
  quantity                  real default 1,
  unit                      varchar(6),
  unit_ratio                real default 1,
  base_price                real default 0,
  price                     real default 0,
  tax_rate                  real default 0,
  tax_rate2                 real default 0,
  tax_rate3                 real default 0,
  discount_rate1            real default 0,
  discount_rate2            real default 0,
  discount_rate3            real default 0,
  amount                    real default 0,
  tax_amount                real default 0,
  discount_amount           real default 0,
  total                     real default 0,
  description               varchar(100),
  trans_year                numeric(4),
  trans_month               varchar(7),
  unit1                     varchar(6),
  unit2                     varchar(6),
  unit3                     varchar(6),
  unit2ratio                real default 0,
  unit3ratio                real default 0,
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  plus_factor_amount        real default 0,
  minus_factor_amount       real default 0,
  serial_no                 varchar(100),
  input                     real default 0,
  output                    real default 0,
  in_total                  real default 0,
  out_total                 real default 0,
  is_return                 boolean default false,
  ret_input                 real default 0,
  ret_output                real default 0,
  ret_in_total              real default 0,
  ret_out_total             real default 0,
  net_input                 real default 0,
  net_output                real default 0,
  net_in_total              real default 0,
  net_out_total             real default 0,
  has_cost_effect           boolean default true,
  trans_id                  integer,
  depot_id                  integer,
  contact_id                integer,
  seller_id                 integer,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  workspace                 integer not null,
  primary key (id)
);
create index stock_trans_detail_ix1 on stock_trans_detail (workspace, trans_date);
create sequence stock_trans_detail_seq;

create table stock_trans_factor (
  id                        serial not null,
  effect                    real default 0,
  quantity                  real default 0,
  amount                    real default 0,
  trans_id                  integer,
  factor_id                 integer,
  primary key (id)
);
create sequence stock_trans_factor_seq;

create table stock_trans_source (
  id                        serial not null,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  has_cost_effect           boolean default true,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index stock_trans_source_ix1 on stock_trans_source (workspace, name);
create sequence stock_trans_source_seq;

create table stock_trans_tax (
  id                        serial not null,
  tax_rate                  real default 0,
  basis                     real default 0,
  amount                    real default 0,
  trans_id                  integer,
  primary key (id)
);
create sequence stock_trans_tax_seq;

create table stock_trans_currency (
  id                        serial not null,
  currency                  varchar(3) default '',
  amount                    real default 0,
  trans_id                  integer,
  primary key (id)
);
create sequence stock_trans_currency_seq;

create table stock_unit (
  id                        serial not null,
  name                      varchar(7) not null,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index stock_unit_ix1 on stock_unit (workspace, name);
create sequence stock_unit_seq;

create table waybill_trans (
  id                        serial not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  is_completed              boolean default false,
  trans_date                date not null,
  real_date                 timestamp,
  delivery_date             timestamp,
  trans_no                  varchar(20),
  is_tax_include            boolean default true,
  rounding_digits           numeric(1),
  total                     real default 0,
  discount_total            real default 0,
  subtotal                  real default 0,
  rounding_discount         real default 0,
  total_discount_rate       real default 0,
  tax_total                 real default 0,
  net_total                 real default 0,
  plus_factor_total         real default 0,
  minus_factor_total        real default 0,
  description               varchar(100),
  trans_year                numeric(4),
  trans_month               varchar(7),
  contact_id                integer,
  contact_name              varchar(100),
  contact_tax_office        varchar(20),
  contact_tax_number        varchar(15),
  contact_address1          varchar(100),
  contact_address2          varchar(100),
  consigner                 varchar(50),
  recepient                 varchar(50),
  trans_type                varchar(6) not null,
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  is_transfer               boolean default false,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  contact_trans_id          integer,
  seller_id                 integer,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  depot_id                  integer,
  invoice_id                integer,
  ref_module                varchar(10),
  ref_id                    integer,
  status_id                 integer,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index waybill_trans_ix1 on waybill_trans (workspace, _right, trans_date);
create sequence waybill_trans_seq;

create table waybill_trans_detail (
  id                        serial not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  delivery_date             date,
  trans_type                varchar(6) not null,
  row_no                    integer,
  stock_id                  integer,
  name                      varchar(100),
  quantity                  real default 1,
  unit                      varchar(6),
  unit_ratio                real default 1,
  base_price                real default 0,
  price                     real default 0,
  tax_rate                  real default 0,
  discount_rate1            real default 0,
  discount_rate2            real default 0,
  discount_rate3            real default 0,
  amount                    real default 0,
  tax_amount                real default 0,
  discount_amount           real default 0,
  total                     real default 0,
  description               varchar(100),
  trans_year                numeric(4),
  trans_month               varchar(7),
  unit1                     varchar(6),
  unit2                     varchar(6),
  unit3                     varchar(6),
  unit2ratio                real default 0,
  unit3ratio                real default 0,
  exc_code                  varchar(3),
  exc_rate                  real default 0,
  exc_equivalent            real default 0,
  plus_factor_amount        real default 0,
  minus_factor_amount       real default 0,
  input                     real default 0,
  output                    real default 0,
  in_total                  real default 0,
  out_total                 real default 0,
  net_input                 real default 0,
  net_output                real default 0,
  net_in_total              real default 0,
  net_out_total             real default 0,
  completed                 real default 0,
  cancelled                 real default 0,
  is_transfer               boolean default false,
  trans_id                  integer,
  depot_id                  integer,
  contact_id                integer,
  seller_id                 integer,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  status_id                 integer,
  workspace                 integer not null,
  primary key (id)
);
create index waybill_trans_detail_ix1 on waybill_trans_detail (workspace, trans_date);
create sequence waybill_trans_detail_seq;

create table waybill_trans_factor (
  id                        serial not null,
  effect                    real default 0,
  amount                    real default 0,
  trans_id                  integer,
  factor_id                 integer,
  primary key (id)
);
create sequence waybill_trans_factor_seq;

create table waybill_trans_relation (
  id                        serial not null,
  rel_id                    integer not null,
  rel_right                 varchar(30) not null,
  rel_receipt_no            integer not null,
  trans_id                  integer,
  primary key (id)
);
create sequence waybill_trans_relation_seq;

create table waybill_trans_source (
  id                        serial not null,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index waybill_trans_source_ix1 on waybill_trans_source (workspace, name);
create sequence waybill_trans_source_seq;

create table waybill_trans_status (
  id                        serial not null,
  parent_id                 integer,
  name                      varchar(30) not null,
  ordering                  integer default 0,
  insert_by                 varchar(20),
  insert_at                 timestamp,
  update_by                 varchar(20),
  update_at                 timestamp,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
);
create index waybill_trans_status_ix1 on waybill_trans_status (name);
create sequence waybill_trans_status_seq;

create table waybill_trans_status_history (
  id                        serial not null,
  trans_time                timestamp,
  trans_id                  integer not null,
  status_id                 integer not null,
  username                  varchar(20),
  description               varchar(150),
  primary key (id)
);
create sequence waybill_trans_status_history_seq;

-- Temp tables

create table temp_contact_aging (
  username                  varchar(20) not null,
  contact_name              varchar(100),
  receipt_no                integer,
  _right                    varchar(50) not null,
  trans_date                date not null,
  trans_no                  varchar(20),
  amount                    real default 0,
  paid                      real default 0,
  remain                    real default 0,
  exc_code                  varchar(3),
  description               varchar(100)
);
create index temp_contact_aging_ix1 on temp_contact_aging (username);
create index temp_contact_aging_ix2 on temp_contact_aging (contact_name);
create index temp_contact_aging_ix3 on temp_contact_aging (trans_date);

-- Special Functions

create function round(float,int) returns numeric as $$
    select round($1::numeric,$2);
$$ language sql immutable;

create function year(date) returns integer as $$
    select extract(year from $1)::integer;
$$ language sql immutable;

-- Relations between tables

alter table bank_trans add foreign key (bank_id) references bank (id);
alter table bank_trans add foreign key (expense_id) references bank_expense (id);
alter table bank_trans add foreign key (trans_source_id) references bank_trans_source (id);
alter table bank_trans add foreign key (trans_point_id) references global_trans_point (id);
alter table bank_trans add foreign key (private_code_id) references global_private_code (id);

alter table chqbll_payroll add foreign key (contact_id) references contact (id);
alter table chqbll_payroll add foreign key (trans_source_id) references chqbll_payroll_source (id);
alter table chqbll_payroll add foreign key (trans_point_id) references global_trans_point (id);
alter table chqbll_payroll add foreign key (private_code_id) references global_private_code (id);

alter table chqbll_payroll_detail add foreign key (cbtype_id) references chqbll_type (id);
alter table chqbll_payroll_detail add foreign key (trans_id) references chqbll_payroll (id);
alter table chqbll_payroll_detail add foreign key (contact_id) references contact (id);
alter table chqbll_payroll_detail add foreign key (trans_source_id) references chqbll_payroll_source (id);
alter table chqbll_payroll_detail add foreign key (trans_point_id) references global_trans_point (id);
alter table chqbll_payroll_detail add foreign key (private_code_id) references global_private_code (id);

alter table chqbll_detail_history add foreign key (contact_id) references contact (id);
alter table chqbll_detail_history add foreign key (bank_id) references bank (id);
alter table chqbll_detail_history add foreign key (safe_id) references safe (id);
alter table chqbll_detail_history add foreign key (detail_id) references chqbll_payroll_detail (id);

alter table chqbll_detail_partial add foreign key (safe_id) references safe (id);
alter table chqbll_detail_partial add foreign key (detail_id) references chqbll_payroll_detail (id);
alter table chqbll_detail_partial add foreign key (trans_id) references safe_trans (id);

alter table chqbll_trans add foreign key (contact_id) references contact (id);
alter table chqbll_trans add foreign key (bank_id) references bank (id);
alter table chqbll_trans add foreign key (safe_id) references safe (id);
alter table chqbll_trans add foreign key (trans_source_id) references chqbll_payroll_source (id);
alter table chqbll_trans add foreign key (trans_point_id) references global_trans_point (id);
alter table chqbll_trans add foreign key (private_code_id) references global_private_code (id);

alter table chqbll_trans_detail add foreign key (trans_id) references chqbll_trans (id);
alter table chqbll_trans_detail add foreign key (detail_id) references chqbll_payroll_detail (id);

alter table contact add foreign key (category_id) references contact_category (id);
alter table contact add foreign key (price_list_id) references stock_price_list (id);
alter table contact add foreign key (seller_id) references sale_seller (id);

alter table contact_extra_fields add foreign key (extra_fields_id) references admin_extra_fields (id);
alter table stock_extra_fields add foreign key (extra_fields_id) references admin_extra_fields (id);

alter table contact_trans add foreign key (contact_id) references contact (id);
alter table contact_trans add foreign key (trans_source_id) references contact_trans_source (id);
alter table contact_trans add foreign key (trans_point_id) references global_trans_point (id);
alter table contact_trans add foreign key (private_code_id) references global_private_code (id);

alter table admin_document_field add foreign key (report_title_doc_id) references admin_document (id);
alter table admin_document_field add foreign key (page_title_doc_id) references admin_document (id);
alter table admin_document_field add foreign key (detail_doc_id) references admin_document (id);
alter table admin_document_field add foreign key (page_footer_doc_id) references admin_document (id);
alter table admin_document_field add foreign key (report_footer_doc_id) references admin_document (id);

alter table invoice_trans add foreign key (contact_id) references contact (id);
alter table invoice_trans add foreign key (depot_id) references stock_depot (id);
alter table invoice_trans add foreign key (trans_source_id) references invoice_trans_source (id);
alter table invoice_trans add foreign key (trans_point_id) references global_trans_point (id);
alter table invoice_trans add foreign key (seller_id) references sale_seller (id);
alter table invoice_trans add foreign key (private_code_id) references global_private_code (id);
alter table invoice_trans add foreign key (status_id) references invoice_trans_status (id);

alter table invoice_trans_detail add foreign key (trans_id) references invoice_trans (id);
alter table invoice_trans_detail add foreign key (stock_id) references stock (id);
alter table invoice_trans_detail add foreign key (depot_id) references stock_depot (id);
alter table invoice_trans_detail add foreign key (contact_id) references contact (id);
alter table invoice_trans_detail add foreign key (seller_id) references sale_seller (id);
alter table invoice_trans_detail add foreign key (trans_source_id) references invoice_trans_source (id);
alter table invoice_trans_detail add foreign key (trans_point_id) references global_trans_point (id);
alter table invoice_trans_detail add foreign key (private_code_id) references global_private_code (id);
alter table invoice_trans_detail add foreign key (status_id) references invoice_trans_status (id);

alter table invoice_trans_factor add foreign key (trans_id) references invoice_trans (id);
alter table invoice_trans_factor add foreign key (factor_id) references stock_cost_factor (id);
alter table invoice_trans_relation add foreign key (trans_id) references invoice_trans (id);
alter table invoice_trans_tax add foreign key (trans_id) references invoice_trans (id);
alter table invoice_trans_currency add foreign key (trans_id) references invoice_trans (id);

alter table invoice_trans_status add foreign key (parent_id) references invoice_trans_status (id);

alter table order_trans add foreign key (contact_id) references contact (id);
alter table order_trans add foreign key (depot_id) references stock_depot (id);
alter table order_trans add foreign key (trans_source_id) references order_trans_source (id);
alter table order_trans add foreign key (trans_point_id) references global_trans_point (id);
alter table order_trans add foreign key (seller_id) references sale_seller (id);
alter table order_trans add foreign key (private_code_id) references global_private_code (id);
alter table order_trans add foreign key (status_id) references order_trans_status (id);

alter table order_trans_detail add foreign key (trans_id) references order_trans (id);
alter table order_trans_detail add foreign key (stock_id) references stock (id);
alter table order_trans_detail add foreign key (depot_id) references stock_depot (id);
alter table order_trans_detail add foreign key (contact_id) references contact (id);
alter table order_trans_detail add foreign key (seller_id) references sale_seller (id);
alter table order_trans_detail add foreign key (trans_source_id) references order_trans_source (id);
alter table order_trans_detail add foreign key (trans_point_id) references global_trans_point (id);
alter table order_trans_detail add foreign key (private_code_id) references global_private_code (id);
alter table order_trans_detail add foreign key (status_id) references order_trans_status (id);

alter table order_trans_factor add foreign key (trans_id) references order_trans (id);
alter table order_trans_factor add foreign key (factor_id) references stock_cost_factor (id);
alter table order_trans_status add foreign key (parent_id) references order_trans_status (id);

alter table sale_campaign add foreign key (stock_category_id) references stock_category (id);

alter table stock add foreign key (category_id) references stock_category (id);
alter table stock_barcode add foreign key (stock_id) references stock (id);

alter table stock_costing add foreign key (trans_point_id) references global_trans_point (id);
alter table stock_costing add foreign key (category_id) references stock_category (id);
alter table stock_costing add foreign key (stock_id) references stock (id);
alter table stock_costing add foreign key (depot_id) references stock_depot (id);

alter table stock_costing_detail add foreign key (costing_id) references stock_costing (id);
alter table stock_costing_detail add foreign key (stock_id) references stock (id);

alter table stock_costing_inventory add foreign key (costing_id) references stock_costing (id);
alter table stock_costing_inventory add foreign key (stock_id) references stock (id);
alter table stock_costing_inventory add foreign key (depot_id) references stock_depot (id);

alter table stock_price_list add foreign key (category_id) references stock_category (id);
alter table stock_price_update add foreign key (category_id) references stock_category (id);
alter table stock_price_update_detail add foreign key (price_update_id) references stock_price_update (id);

alter table stock_trans add foreign key (contact_id) references contact (id);
alter table stock_trans add foreign key (depot_id) references stock_depot (id);
alter table stock_trans add foreign key (ref_depot_id) references stock_depot (id);
alter table stock_trans add foreign key (trans_source_id) references stock_trans_source (id);
alter table stock_trans add foreign key (trans_point_id) references global_trans_point (id);
alter table stock_trans add foreign key (seller_id) references sale_seller (id);
alter table stock_trans add foreign key (private_code_id) references global_private_code (id);

alter table stock_trans_detail add foreign key (trans_id) references stock_trans (id);
alter table stock_trans_detail add foreign key (stock_id) references stock (id);
alter table stock_trans_detail add foreign key (depot_id) references stock_depot (id);
alter table stock_trans_detail add foreign key (contact_id) references contact (id);
alter table stock_trans_detail add foreign key (seller_id) references sale_seller (id);
alter table stock_trans_detail add foreign key (trans_source_id) references stock_trans_source (id);
alter table stock_trans_detail add foreign key (trans_point_id) references global_trans_point (id);
alter table stock_trans_detail add foreign key (private_code_id) references global_private_code (id);

alter table stock_trans_factor add foreign key (trans_id) references stock_trans (id);
alter table stock_trans_factor add foreign key (factor_id) references stock_cost_factor (id);
alter table stock_trans_tax add foreign key (trans_id) references stock_trans (id);
alter table stock_trans_currency add foreign key (trans_id) references stock_trans (id);

alter table safe_trans add foreign key (safe_id) references safe (id);
alter table safe_trans add foreign key (expense_id) references safe_expense (id);
alter table safe_trans add foreign key (trans_source_id) references safe_trans_source (id);
alter table safe_trans add foreign key (trans_point_id) references global_trans_point (id);
alter table safe_trans add foreign key (private_code_id) references global_private_code (id);

alter table waybill_trans add foreign key (contact_id) references contact (id);
alter table waybill_trans add foreign key (depot_id) references stock_depot (id);
alter table waybill_trans add foreign key (trans_source_id) references waybill_trans_source (id);
alter table waybill_trans add foreign key (trans_point_id) references global_trans_point (id);
alter table waybill_trans add foreign key (seller_id) references sale_seller (id);
alter table waybill_trans add foreign key (private_code_id) references global_private_code (id);
alter table waybill_trans add foreign key (status_id) references waybill_trans_status (id);

alter table waybill_trans_detail add foreign key (trans_id) references waybill_trans (id);
alter table waybill_trans_detail add foreign key (stock_id) references stock (id);
alter table waybill_trans_detail add foreign key (depot_id) references stock_depot (id);
alter table waybill_trans_detail add foreign key (contact_id) references contact (id);
alter table waybill_trans_detail add foreign key (seller_id) references sale_seller (id);
alter table waybill_trans_detail add foreign key (trans_source_id) references waybill_trans_source (id);
alter table waybill_trans_detail add foreign key (trans_point_id) references global_trans_point (id);
alter table waybill_trans_detail add foreign key (private_code_id) references global_private_code (id);
alter table waybill_trans_detail add foreign key (status_id) references waybill_trans_status (id);

alter table waybill_trans_factor add foreign key (trans_id) references waybill_trans (id);
alter table waybill_trans_factor add foreign key (factor_id) references stock_cost_factor (id);
alter table waybill_trans_relation add foreign key (trans_id) references waybill_trans (id);

alter table waybill_trans_status add foreign key (parent_id) references waybill_trans_status (id);

alter table admin_user_right add foreign key (user_role_id) references admin_user_role (id);
alter table admin_user add foreign key (user_group_id) references admin_user_group (id);

alter table admin_user_given_role add foreign key (user_group_id) references admin_user_group (id);
alter table admin_user_given_role add foreign key (workspace_id) references admin_workspace (id);
alter table admin_user_given_role add foreign key (user_role_id) references admin_user_role (id);

alter table global_currency_rate_detail add foreign key (currency_rate_id) references global_currency_rate (id);

alter table contact add foreign key (extra_field0_id) references contact_extra_fields (id);
alter table contact add foreign key (extra_field1_id) references contact_extra_fields (id);
alter table contact add foreign key (extra_field2_id) references contact_extra_fields (id);
alter table contact add foreign key (extra_field3_id) references contact_extra_fields (id);
alter table contact add foreign key (extra_field4_id) references contact_extra_fields (id);
alter table contact add foreign key (extra_field5_id) references contact_extra_fields (id);
alter table contact add foreign key (extra_field6_id) references contact_extra_fields (id);
alter table contact add foreign key (extra_field7_id) references contact_extra_fields (id);
alter table contact add foreign key (extra_field8_id) references contact_extra_fields (id);
alter table contact add foreign key (extra_field9_id) references contact_extra_fields (id);

alter table stock add foreign key (extra_field0_id) references stock_extra_fields (id);
alter table stock add foreign key (extra_field1_id) references stock_extra_fields (id);
alter table stock add foreign key (extra_field2_id) references stock_extra_fields (id);
alter table stock add foreign key (extra_field3_id) references stock_extra_fields (id);
alter table stock add foreign key (extra_field4_id) references stock_extra_fields (id);
alter table stock add foreign key (extra_field5_id) references stock_extra_fields (id);
alter table stock add foreign key (extra_field6_id) references stock_extra_fields (id);
alter table stock add foreign key (extra_field7_id) references stock_extra_fields (id);
alter table stock add foreign key (extra_field8_id) references stock_extra_fields (id);
alter table stock add foreign key (extra_field9_id) references stock_extra_fields (id);

alter table sale_campaign add foreign key (extra_field0_id) references stock_extra_fields (id);
alter table sale_campaign add foreign key (extra_field1_id) references stock_extra_fields (id);
alter table sale_campaign add foreign key (extra_field2_id) references stock_extra_fields (id);
alter table sale_campaign add foreign key (extra_field3_id) references stock_extra_fields (id);
alter table sale_campaign add foreign key (extra_field4_id) references stock_extra_fields (id);
alter table sale_campaign add foreign key (extra_field5_id) references stock_extra_fields (id);
alter table sale_campaign add foreign key (extra_field6_id) references stock_extra_fields (id);
alter table sale_campaign add foreign key (extra_field7_id) references stock_extra_fields (id);
alter table sale_campaign add foreign key (extra_field8_id) references stock_extra_fields (id);
alter table sale_campaign add foreign key (extra_field9_id) references stock_extra_fields (id);

alter table stock_costing add foreign key (extra_field0_id) references stock_extra_fields (id);
alter table stock_costing add foreign key (extra_field1_id) references stock_extra_fields (id);
alter table stock_costing add foreign key (extra_field2_id) references stock_extra_fields (id);
alter table stock_costing add foreign key (extra_field3_id) references stock_extra_fields (id);
alter table stock_costing add foreign key (extra_field4_id) references stock_extra_fields (id);
alter table stock_costing add foreign key (extra_field5_id) references stock_extra_fields (id);
alter table stock_costing add foreign key (extra_field6_id) references stock_extra_fields (id);
alter table stock_costing add foreign key (extra_field7_id) references stock_extra_fields (id);
alter table stock_costing add foreign key (extra_field8_id) references stock_extra_fields (id);
alter table stock_costing add foreign key (extra_field9_id) references stock_extra_fields (id);

alter table stock_price_update add foreign key (extra_field0_id) references stock_extra_fields (id);
alter table stock_price_update add foreign key (extra_field1_id) references stock_extra_fields (id);
alter table stock_price_update add foreign key (extra_field2_id) references stock_extra_fields (id);
alter table stock_price_update add foreign key (extra_field3_id) references stock_extra_fields (id);
alter table stock_price_update add foreign key (extra_field4_id) references stock_extra_fields (id);
alter table stock_price_update add foreign key (extra_field5_id) references stock_extra_fields (id);
alter table stock_price_update add foreign key (extra_field6_id) references stock_extra_fields (id);
alter table stock_price_update add foreign key (extra_field7_id) references stock_extra_fields (id);
alter table stock_price_update add foreign key (extra_field8_id) references stock_extra_fields (id);
alter table stock_price_update add foreign key (extra_field9_id) references stock_extra_fields (id);

alter table stock_price_list add foreign key (extra_field0_id) references stock_extra_fields (id);
alter table stock_price_list add foreign key (extra_field1_id) references stock_extra_fields (id);
alter table stock_price_list add foreign key (extra_field2_id) references stock_extra_fields (id);
alter table stock_price_list add foreign key (extra_field3_id) references stock_extra_fields (id);
alter table stock_price_list add foreign key (extra_field4_id) references stock_extra_fields (id);
alter table stock_price_list add foreign key (extra_field5_id) references stock_extra_fields (id);
alter table stock_price_list add foreign key (extra_field6_id) references stock_extra_fields (id);
alter table stock_price_list add foreign key (extra_field7_id) references stock_extra_fields (id);
alter table stock_price_list add foreign key (extra_field8_id) references stock_extra_fields (id);
alter table stock_price_list add foreign key (extra_field9_id) references stock_extra_fields (id);

-- INSTANT DATA

-- super and admin user passwords are 1234

insert into admin_user (id, username, title, password_hash, is_admin, is_active) values (1, 'super', 'super user', '81dc9bdb52d04dc20036dbd8313ed055', true, true);
insert into admin_user (id, username, title, password_hash, is_admin, is_active) values (2, 'admin', 'administrator', '81dc9bdb52d04dc20036dbd8313ed055', true, true);

insert into admin_workspace (id, name, description) values (1, 'seyhan', 'seyhan çalışma dönemi');

insert into contact_category (id, name, working_dir, debt_limit, credit_limit, workspace, insert_by, insert_at, is_active) values (1, 'Genel', null, 0, 0, 1, 'admin', NOW(), true);
insert into contact_category (id, name, working_dir, debt_limit, credit_limit, workspace, insert_by, insert_at, is_active) values (2, 'Müşteri', 'Debt', 0, 0, 1, 'admin', NOW(), true);
insert into contact_category (id, name, working_dir, debt_limit, credit_limit, workspace, insert_by, insert_at, is_active) values (3, 'Satıcı', 'Credit', 0, 0, 1, 'admin', NOW(), true);
insert into contact_category (id, name, working_dir, debt_limit, credit_limit, workspace, insert_by, insert_at, is_active) values (4, 'Dağıtıcı', null, 0, 0, 1, 'admin', NOW(), true);
insert into contact_category (id, name, working_dir, debt_limit, credit_limit, workspace, insert_by, insert_at, is_active) values (5, 'Bayi', null, 0, 0, 1, 'admin', NOW(), true);

insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (1, 'USD', 'ABD DOLARI', 'admin', NOW(), true);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (2, 'EUR', 'EURO', 'admin', NOW(), true);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (3, 'CNY', 'ÇİN YUANI', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (4, 'RUB', 'RUS RUBLESİ', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (5, 'SAR', 'SUUDİ ARABİSTAN RİYALİ', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (6, 'JPY', 'JAPON YENİ', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (7, 'GBP', 'İNGİLİZ STERLİNİ', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (8, 'IRR', 'İRAN RİYALİ', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (9, 'CAD', 'KANADA DOLARI', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (10, 'KWD', 'KUVEYT DİNARI', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (11, 'AUD', 'AVUSTRALYA DOLARI', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (12, 'DKK', 'DANİMARKA KRONU', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (13, 'CHF', 'İSVİÇRE FRANGI', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (14, 'SEK', 'İSVEÇ KRONU', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (15, 'NOK', 'NORVEÇ KRONU', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (16, 'BGN', 'BULGAR LEVASI', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (17, 'RON', 'RUMEN LEYİ', 'admin', NOW(), false);
insert into global_currency (id, code, name, insert_by, insert_at, is_active) values (18, 'PKR', 'PAKİSTAN RUPİSİ', 'admin', NOW(), false);

insert into bank_trans_source (id, name, suitable_right, workspace, insert_by, insert_at, is_active) values (1, 'BORÇ AÇILIŞ İŞLEMI', 'BANK_HESABA_PARA_GIRISI', 1, 'admin', NOW(), true);
insert into bank_trans_source (id, name, suitable_right, workspace, insert_by, insert_at, is_active) values (2, 'ALACAK AÇILIŞ İŞLEMI', 'BANK_HESAPTAN_PARA_CIKISI', 1, 'admin', NOW(), true);
insert into bank_trans_source (id, name, suitable_right, workspace, insert_by, insert_at, is_active) values (3, 'GELEN EFT', 'BANK_HESABA_PARA_GIRISI', 1, 'admin', NOW(), true);
insert into bank_trans_source (id, name, suitable_right, workspace, insert_by, insert_at, is_active) values (4, 'GÖNDERILEN EFT', 'BANK_HESAPTAN_PARA_CIKISI', 1, 'admin', NOW(), true);
insert into bank_trans_source (id, name, suitable_right, workspace, insert_by, insert_at, is_active) values (5, 'GELEN HAVALE', 'BANK_HESABA_PARA_GIRISI', 1, 'admin', NOW(), true);
insert into bank_trans_source (id, name, suitable_right, workspace, insert_by, insert_at, is_active) values (6, 'GÖNDERILEN HAVALE', 'BANK_HESAPTAN_PARA_CIKISI', 1, 'admin', NOW(), true);
insert into bank_trans_source (id, name, suitable_right, workspace, insert_by, insert_at, is_active) values (7, 'ELDEN YATIRILAN', 'BANK_HESABA_PARA_GIRISI', 1, 'admin', NOW(), true);
insert into bank_trans_source (id, name, suitable_right, workspace, insert_by, insert_at, is_active) values (8, 'ELDEN ÇEKILEN', 'BANK_HESAPTAN_PARA_CIKISI', 1, 'admin', NOW(), true);

insert into contact_trans_source (id, name, suitable_right, workspace, insert_by, insert_at, is_active) values (1, 'BORÇ AÇILIŞ İŞLEMI', 'CARI_BORC_DEKONTU', 1, 'admin', NOW(), true);
insert into contact_trans_source (id, name, suitable_right, workspace, insert_by, insert_at, is_active) values (2, 'ALACAK AÇILIŞ İŞLEMI', 'CARI_ALACAK_DEKONTU', 1, 'admin', NOW(), true);
insert into safe_trans_source (id, name, suitable_right, workspace, insert_by, insert_at, is_active) values (1, 'BORÇ AÇILIŞ İŞLEMI', 'KASA_MAHSUP_FISI', 1, 'admin', NOW(), true);
insert into safe_trans_source (id, name, suitable_right, workspace, insert_by, insert_at, is_active) values (2, 'ALACAK AÇILIŞ İŞLEMI', 'KASA_MAHSUP_FISI', 1, 'admin', NOW(), true);
insert into stock_trans_source (id, name, suitable_right, has_cost_effect, workspace, insert_by, insert_at, is_active) values (1, 'AÇILIŞ FIŞI', 'STOK_GIRIS_FISI', true, 1, 'admin', NOW(), true);
insert into invoice_trans_source (id, name, suitable_right, has_cost_effect, has_stock_effect, workspace, insert_by, insert_at, is_active) values (1, 'HIZMET FATURASI', null, false, false, 1, 'admin', NOW(), true);

insert into stock_unit (id, name, workspace, insert_by, insert_at, is_active) values (1, 'ADET', 1, 'admin', NOW(), true);
insert into stock_unit (id, name, workspace, insert_by, insert_at, is_active) values (2, 'PAKET', 1, 'admin', NOW(), true);
insert into stock_unit (id, name, workspace, insert_by, insert_at, is_active) values (3, 'KOLI', 1, 'admin', NOW(), true);
insert into stock_unit (id, name, workspace, insert_by, insert_at, is_active) values (4, 'KG', 1, 'admin', NOW(), false);
insert into stock_unit (id, name, workspace, insert_by, insert_at, is_active) values (5, 'GR', 1, 'admin', NOW(), false);

insert into safe (id, name, workspace, insert_by, insert_at, is_active) values (1, 'MERKEZ KASA', 1, 'admin', NOW(), true);
insert into stock_depot (id, name, workspace, insert_by, insert_at, is_active) values (1, 'MERKEZ DEPO', 1, 'admin', NOW(), true);
insert into global_trans_point (id, name, workspace, insert_by, insert_at) values (1, 'GENEL', 1, 'admin', NOW());

insert into admin_document_target (id, name, is_local, target_type, view_type, path, is_compressed, description, is_active) values (1, 'local_file', true, 'FILE', 'PORTRAIT', '//opt/', true, 'Yerel dosya', true);
insert into admin_document_target (id, name, is_local, target_type, view_type, path, is_compressed, description, is_active) values (2, 'local_printer', true, 'DOT_MATRIX', 'PORTRAIT', 'LPT1:', true, 'Yerel Nokta Vuruşlu yazıcı', true);
insert into admin_document_target (id, name, is_local, target_type, view_type, path, is_compressed, description, is_active) values (3, 'local_lazer', true, 'LASER', 'PORTRAIT', '', true, 'Yerel Lazer yazıcı', true);
insert into admin_document_target (id, name, is_local, target_type, view_type, path, is_compressed, description, is_active) values (4, 'remote_file', false, 'FILE', 'PORTRAIT', '//opt/', true, 'Uzak dosya', false);
insert into admin_document_target (id, name, is_local, target_type, view_type, path, is_compressed, description, is_active) values (5, 'remote_printer', false, 'DOT_MATRIX', 'PORTRAIT', 'LPT1:', true, 'Uzak Nokta Vuruşlu yazıcı', false);
insert into admin_document_target (id, name, is_local, target_type, view_type, path, is_compressed, description, is_active) values (6, 'remote_lazer', false, 'LASER', 'PORTRAIT', '', true, 'Uzak Lazer yazıcı', false);

insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (1, 0, 'contact', 'Gurup', false, true);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (2, 1, 'contact', 'ek_alan1', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (3, 2, 'contact', 'ek_alan2', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (4, 3, 'contact', 'ek_alan3', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (5, 4, 'contact', 'ek_alan4', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (6, 5, 'contact', 'ek_alan5', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (7, 6, 'contact', 'ek_alan6', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (8, 7, 'contact', 'ek_alan7', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (9, 8, 'contact', 'ek_alan8', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (10,9, 'contact', 'ek_alan9', false, false);

insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (11, 0, 'stock', 'Gurup', false, true);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (12, 1, 'stock', 'Marka', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (13, 2, 'stock', 'Model', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (14, 3, 'stock', 'Raf', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (15, 4, 'stock', 'Renk', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (16, 5, 'stock', 'Beden', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (17, 6, 'stock', 'Boy', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (18, 7, 'stock', 'ek_alan7', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (19, 8, 'stock', 'ek_alan8', false, false);
insert into admin_extra_fields (id, idno, distinction, name, is_required, is_active) values (20, 9, 'stock', 'ek_alan9', false, false);

COMMIT;