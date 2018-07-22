-- FIRST INIT

-- create database seyhan;

-- use seyhan;

BEGIN TRANSACTION;

create table admin_document (
  id                        integer identity(1,1) primary key,
  module                    varchar(10) not null,
  header                    varchar(20),
  _right                    varchar(50) not null,
  name                      varchar(20) not null,
  page_rows                 smallint default 66,
  report_title_rows         smallint default 0,
  page_title_rows           smallint default 3,
  detail_rows               smallint default 1,
  page_footer_rows          smallint default 3,
  report_footer_rows        smallint default 0,
  report_title_labels       bit default 1,
  page_title_labels         bit default 1,
  detail_labels             bit default 1,
  page_footer_labels        bit default 1,
  report_footer_labels      bit default 1,
  left_margin               smallint default 0,
  top_margin                smallint default 0,
  bottom_margin             smallint default 0,
  is_single_page            bit default 0,
  has_paging                bit default 1,
  column_title_type         varchar(7),
  carrying_over_name        varchar(50),
  description               varchar(30),
  template_rows             text,
  is_active                 bit default 1,
  version                   integer default 0
);
create unique index admin_document_ix1 on admin_document (name);

create table admin_document_field (
  id                        integer identity(1,1) primary key,
  module                    varchar(10),
  band                      varchar(12),
  _type                     varchar(20),
  name                      varchar(100),
  nick_name                 varchar(100),
  hidden_field              varchar(100),
  _label                    varchar(70),
  original_label            varchar(70),
  label_width               smallint,
  label_align               varchar(5),
  _width                    smallint,
  _row                      smallint,
  _column                   smallint,
  _format                   varchar(30),
  prefix                    varchar(5),
  suffix                    varchar(5),
  _value                    varchar(70),
  msg_prefix                varchar(30),
  defauld                   varchar(50),
  is_db_field               bit default 1,
  table_type                varchar(10),
  report_title_doc_id       integer,
  page_title_doc_id         integer,
  detail_doc_id             integer,
  page_footer_doc_id        integer,
  report_footer_doc_id      integer
);

create table admin_document_target (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  is_local                  bit default 1,
  target_type               varchar(10),
  view_type                 varchar(9),
  path                      varchar(150),
  is_compressed             bit default 1,
  description               varchar(30),
  is_active                 bit default 1,
  version                   integer default 0
);
create unique index admin_document_target_ix1 on admin_document_target (name);

create table admin_extra_fields (
  id                        integer identity(1,1) primary key,
  idno                      integer not null,
  distinction               varchar(15) not null,
  name                      varchar(12) not null,
  is_required               bit default 0,
  is_active                 bit default 1
);

create table admin_setting (
  id                        integer identity(1,1) primary key,
  code                      varchar(10) not null,
  description               varchar(30),
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  json_data                 text,
  version                   integer default 0
);
create unique index admin_setting_ix1 on admin_setting (code);

create table admin_user (
  id                        integer identity(1,1) primary key,
  username                  varchar(20) not null,
  title                     varchar(30),
  email                     varchar(100),
  auth_token                varchar(32),
  password_hash             varchar(60),
  is_admin                  bit default 0,
  is_active                 bit default 1,
  profile                   varchar(20),
  workspace                 integer,
  user_group_id             integer,
  version                   integer default 0
);
create unique index admin_user_ix1 on admin_user (username);

create table admin_user_audit (
  id                        integer identity(1,1) primary key,
  username                  varchar(20),
  _date                     datetime,
  _right                    varchar(30),
  ip                        varchar(45),
  description               varchar(255),
  log_level                 varchar(7),
  workspace                 varchar(30)
);
create index admin_user_audit_ix1 on admin_user_audit (workspace, _date, username);

create table admin_user_given_role (
  id                        integer identity(1,1) primary key,
  user_group_id             integer,
  workspace_id              integer,
  user_role_id              integer
);

create table admin_user_group (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  description               varchar(50),
  editing_timeout           smallint default 0,
  editing_limit             varchar(10),
  has_edit_dif_date         bit default 0,
  version                   integer default 0
);
create unique index admin_user_group_ix1 on admin_user_group (name);

create table admin_user_right (
  id                        integer identity(1,1) primary key,
  name                      varchar(50) not null,
  right_level               varchar(7) not null,
  is_crud                   bit default 0,
  user_role_id              integer
);
create index admin_user_right_ix1 on admin_user_right (name);

create table admin_user_role (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  version                   integer default 0
);
create unique index admin_user_role_ix1 on admin_user_role (name);

create table admin_workspace (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  description               varchar(50),
  start_date                date,
  end_date                  date,
  has_date_restriction      bit default 0,
  is_active                 bit default 1,
  version                   integer default 0
);
create unique index admin_workspace_ix1 on admin_workspace (name);

create table bank (
  id                        integer identity(1,1) primary key,
  account_no                varchar(26) not null,
  name                      varchar(50) not null,
  branch                    varchar(30),
  city                      varchar(20),
  iban                      varchar(26),
  exc_code                  varchar(3),
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index bank_ix1 on bank (workspace, name);

create table bank_expense (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index bank_expense_ix1 on bank_expense (workspace, name);

create table bank_trans (
  id                        integer identity(1,1) primary key,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  trans_no                  varchar(20),
  trans_type                varchar(6) not null,
  trans_dir                 smallint default 0,
  amount                    float default 0 not null,
  debt                      float default 0 not null,
  credit                    float default 0 not null,
  description               varchar(100),
  trans_year                smallint,
  trans_month               varchar(7),
  exc_code                  varchar(3),
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  expense_amount            float default 0,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  bank_id                   integer not null,
  expense_id                integer,
  ref_module                varchar(10),
  ref_id                    integer,
  workspace                 integer not null,
  version                   integer default 0
);
create index bank_trans_ix1 on bank_trans (workspace, _right, trans_date);

create table bank_trans_source (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index bank_trans_source_ix1 on bank_trans_source (workspace, name);

create table chqbll_payroll (
  id                        integer identity(1,1) primary key,
  sort                      varchar(6) not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  trans_no                  varchar(20),
  trans_type                varchar(6) not null,
  total                     float default 0 not null,
  row_count                 integer default 0 not null,
  adat                      integer default 0 not null,
  avarage_date              date not null,
  description               varchar(100),
  trans_year                smallint,
  trans_month               varchar(7),
  exc_code                  varchar(3),
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  contact_id                integer,
  ref_module                varchar(10),
  ref_id                    integer,
  workspace                 integer not null,
  version                   integer default 0
);
create index chqbll_payroll_ix1 on chqbll_payroll (workspace, sort, _right, trans_date);

create table chqbll_payroll_detail (
  id                        integer identity(1,1) primary key,
  sort                      varchar(6) not null,
  is_customer               bit default 1,
  portfolio_no              integer not null,
  row_no                    integer not null,
  serial_no                 varchar(25),
  due_date                  date not null,
  amount                    float default 0 not null,
  description               varchar(100),
  due_year                  smallint,
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
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  total_paid                float default 0,
  cbtype_id                 integer,
  trans_id                  integer,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  contact_id                integer,
  bank_id                   integer,
  workspace                 integer not null
);
create index chqbll_payroll_detail_ix1 on chqbll_payroll_detail (workspace, is_customer, sort, due_date, last_step);

create table chqbll_detail_history (
  id                        integer identity(1,1) primary key,
  sort                      varchar(6) not null,
  step_date                 date not null,
  step                      varchar(15) not null,
  insert_by                 varchar(20),
  insert_at                 datetime,
  detail_id                 integer,
  contact_id                integer,
  bank_id                   integer,
  safe_id                   integer
);
create index chqbll_detail_history_ix1 on chqbll_detail_history (sort, step_date);

create table chqbll_detail_partial (
  id                        integer identity(1,1) primary key,
  sort                      varchar(6) not null,
  is_customer               bit default 1,
  trans_date                date not null,
  amount                    float default 0 not null,
  exc_code                  varchar(3),
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  description               varchar(100),
  insert_by                 varchar(20),
  insert_at                 datetime,
  detail_id                 integer,
  safe_id                   integer,
  trans_id                  integer
);
create index chqbll_detail_partial_ix1 on chqbll_detail_partial (sort, is_customer, trans_date);

create table chqbll_payroll_source (
  id                        integer identity(1,1) primary key,
  sort                      varchar(6) not null,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index chqbll_payroll_source_ix1 on chqbll_payroll_source (workspace, name);

create table chqbll_trans (
  id                        integer identity(1,1) primary key,
  sort                      varchar(6) not null,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  from_step                 varchar(15) not null,
  to_step                   varchar(15) not null,
  trans_date                date not null,
  trans_no                  varchar(20),
  trans_type                varchar(6) not null,
  total                     float default 0 not null,
  row_count                 integer default 0 not null,
  adat                      integer default 0 not null,
  avarage_date              date not null,
  description               varchar(100),
  trans_year                smallint,
  trans_month               varchar(7),
  exc_code                  varchar(3),
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  contact_id                integer,
  bank_id                   integer,
  safe_id                   integer,
  ref_module                varchar(10),
  ref_id                    integer,
  workspace                 integer not null,
  version                   integer default 0
);
create index chqbll_trans_ix1 on chqbll_trans (workspace, sort, _right, trans_date);

create table chqbll_trans_detail (
  id                        integer identity(1,1) primary key,
  trans_id                  integer,
  detail_id                 integer
);

create table chqbll_type (
  id                        integer identity(1,1) primary key,
  sort                      varchar(6) not null,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index chqbll_type_ix1 on chqbll_type (workspace, name);

create table contact (
  id                        integer identity(1,1) primary key,
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
  is_active                 bit default 1,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
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
  version                   integer default 0
);
create index contact_ix1 on contact (workspace, name);
create index contact_ix2 on contact (workspace, code);

create table contact_category (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  working_dir               varchar(6),
  debt_limit                float default 0,
  credit_limit              float default 0,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index contact_category_ix1 on contact_category (workspace, name);

create table contact_extra_fields (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  extra_fields_id           integer,
  workspace                 integer not null,
  version                   integer default 0
);
create index contact_extra_fields_ix1 on contact_extra_fields (workspace, extra_fields_id, name);

create table contact_trans (
  id                        integer identity(1,1) primary key,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  maturity                  date,
  trans_no                  varchar(20),
  trans_type                varchar(6) not null,
  trans_dir                 smallint default 0,
  amount                    float default 0 not null,
  debt                      float default 0 not null,
  credit                    float default 0 not null,
  description               varchar(100),
  trans_year                smallint,
  trans_month               varchar(7),
  exc_code                  varchar(3),
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  contact_id                integer not null,
  ref_module                varchar(10),
  ref_id                    integer,
  workspace                 integer not null,
  version                   integer default 0
);
create index contact_trans_ix1 on contact_trans (workspace, _right, trans_date);

create table contact_trans_source (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index contact_trans_source_ix1 on contact_trans_source (workspace, name);

create table global_currency (
  id                        integer identity(1,1) primary key,
  code                      varchar(3) not null,
  name                      varchar(25) not null,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 0,
  version                   integer default 0
);
create unique index global_currency_ix1 on global_currency (code);

create table global_currency_rate (
  id                        integer identity(1,1) primary key,
  _date                     date not null,
  source                    varchar(100),
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  version                   integer default 0
);
create unique index global_currency_rate_ix1 on global_currency_rate (_date);

create table global_currency_rate_detail (
  id                        integer identity(1,1) primary key,
  _date                     date not null,
  code                      varchar(3) not null,
  name                      varchar(25) not null,
  buying                    float default 1,
  selling                   float default 1,
  currency_rate_id          integer
);
create unique index global_currency_rate_detail_ix1 on global_currency_rate_detail (_date, code);

create table global_private_code (
  id                        integer identity(1,1) primary key,
  par1id                    integer,
  par2id                    integer,
  par3id                    integer,
  par4id                    integer,
  par5id                    integer,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  workspace                 integer not null,
  version                   integer default 0
);
create index global_private_code_ix1 on global_private_code (workspace, name);

create table global_profile (
  id                        integer identity(1,1) primary key,
  name                      varchar(20) not null,
  description               varchar(30),
  is_active                 bit default 1,
  json_data                 text,
  version                   integer default 0
);
create unique index global_profile_ix1 on global_profile (name);

create table global_trans_point (
  id                        integer identity(1,1) primary key,
  par1id                    integer,
  par2id                    integer,
  par3id                    integer,
  par4id                    integer,
  par5id                    integer,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  workspace                 integer not null,
  version                   integer default 0
);
create index global_trans_point_ix1 on global_trans_point (workspace, name);

create table invoice_trans (
  id                        integer identity(1,1) primary key,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  is_cash                   bit default 1,
  is_completed              bit default 0,
  trans_date                date not null,
  real_date                 datetime,
  delivery_date             datetime,
  trans_no                  varchar(20),
  is_tax_include            bit default 1,
  rounding_digits           bit,
  total                     float default 0,
  discount_total            float default 0,
  subtotal                  float default 0,
  rounding_discount         float default 0,
  total_discount_rate       float default 0,
  tax_total                 float default 0,
  net_total                 float default 0,
  plus_factor_total         float default 0,
  minus_factor_total        float default 0,
  withholding_rate          float default 0,
  withholding_before        float default 0,
  withholding_amount        float default 0,
  withholding_after         float default 0,
  description               varchar(100),
  trans_year                smallint,
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
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
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
  version                   integer default 0
);
create index invoice_trans_ix1 on invoice_trans (workspace, _right, trans_date);

create table invoice_trans_detail (
  id                        integer identity(1,1) primary key,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  delivery_date             date,
  trans_type                varchar(6) not null,
  row_no                    integer,
  stock_id                  integer,
  name                      varchar(100),
  quantity                  float default 1,
  unit                      varchar(6),
  unit_ratio                float default 1,
  base_price                float default 0,
  price                     float default 0,
  tax_rate                  float default 0,
  tax_rate2                 float default 0,
  tax_rate3                 float default 0,
  discount_rate1            float default 0,
  discount_rate2            float default 0,
  discount_rate3            float default 0,
  amount                    float default 0,
  tax_amount                float default 0,
  discount_amount           float default 0,
  total                     float default 0,
  description               varchar(100),
  trans_year                smallint,
  trans_month               varchar(7),
  unit1                     varchar(6),
  unit2                     varchar(6),
  unit3                     varchar(6),
  unit2ratio                float default 0,
  unit3ratio                float default 0,
  exc_code                  varchar(3),
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  plus_factor_amount        float default 0,
  minus_factor_amount       float default 0,
  serial_no                 varchar(100),
  input                     float default 0,
  output                    float default 0,
  in_total                  float default 0,
  out_total                 float default 0,
  is_return                 bit default 0,
  ret_input                 float default 0,
  ret_output                float default 0,
  ret_in_total              float default 0,
  ret_out_total             float default 0,
  net_input                 float default 0,
  net_output                float default 0,
  net_in_total              float default 0,
  net_out_total             float default 0,
  has_cost_effect           bit default 1,
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
  workspace                 integer not null
);
create index invoice_trans_detail_ix1 on invoice_trans_detail (workspace, trans_date);

create table invoice_trans_factor (
  id                        integer identity(1,1) primary key,
  effect                    float default 0,
  amount                    float default 0,
  trans_id                  integer,
  factor_id                 integer
);

create table invoice_trans_relation (
  id                        integer identity(1,1) primary key,
  rel_id                    integer not null,
  rel_right                 varchar(30) not null,
  rel_receipt_no            integer not null,
  trans_id                  integer
);

create table invoice_trans_source (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  has_cost_effect           bit default 1,
  has_stock_effect          bit default 1,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index invoice_trans_source_ix1 on invoice_trans_source (workspace, name);

create table invoice_trans_status (
  id                        integer identity(1,1) primary key,
  parent_id                 integer,
  name                      varchar(30) not null,
  ordering                  integer default 0,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0
);
create index invoice_trans_status_ix1 on invoice_trans_status (name);

create table invoice_trans_status_history (
  id                        integer identity(1,1) primary key,
  trans_time                datetime,
  trans_id                  integer not null,
  status_id                 integer not null,
  username                  varchar(20),
  description               varchar(150)
);

create table invoice_trans_tax (
  id                        integer identity(1,1) primary key,
  tax_rate                  float default 0,
  basis                     float default 0,
  amount                    float default 0,
  trans_id                  integer
);

create table invoice_trans_currency (
  id                        integer identity(1,1) primary key,
  currency                  varchar(3) default '',
  amount                    float default 0,
  trans_id                  integer
);

create table order_trans (
  id                        integer identity(1,1) primary key,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  is_completed              bit default 0,
  trans_date                date not null,
  real_date                 datetime,
  delivery_date             datetime,
  trans_no                  varchar(20),
  is_tax_include            bit default 1,
  rounding_digits           bit,
  total                     float default 0,
  discount_total            float default 0,
  subtotal                  float default 0,
  rounding_discount         float default 0,
  total_discount_rate       float default 0,
  tax_total                 float default 0,
  net_total                 float default 0,
  plus_factor_total         float default 0,
  minus_factor_total        float default 0,
  description               varchar(100),
  trans_year                smallint,
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
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  is_transfer               bit default 0,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
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
  version                   integer default 0
);
create index order_trans_ix1 on order_trans (workspace, _right, trans_date);

create table order_trans_detail (
  id                        integer identity(1,1) primary key,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  delivery_date             date,
  trans_type                varchar(6) not null,
  row_no                    integer,
  stock_id                  integer,
  name                      varchar(100),
  quantity                  float default 1,
  unit                      varchar(6),
  unit_ratio                float default 1,
  base_price                float default 0,
  price                     float default 0,
  tax_rate                  float default 0,
  discount_rate1            float default 0,
  discount_rate2            float default 0,
  discount_rate3            float default 0,
  amount                    float default 0,
  tax_amount                float default 0,
  discount_amount           float default 0,
  total                     float default 0,
  description               varchar(100),
  trans_year                smallint,
  trans_month               varchar(7),
  unit1                     varchar(6),
  unit2                     varchar(6),
  unit3                     varchar(6),
  unit2ratio                float default 0,
  unit3ratio                float default 0,
  exc_code                  varchar(3),
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  plus_factor_amount        float default 0,
  minus_factor_amount       float default 0,
  input                     float default 0,
  output                    float default 0,
  in_total                  float default 0,
  out_total                 float default 0,
  net_input                 float default 0,
  net_output                float default 0,
  net_in_total              float default 0,
  net_out_total             float default 0,
  completed                 float default 0,
  cancelled                 float default 0,
  is_transfer               bit default 0,
  trans_id                  integer,
  depot_id                  integer,
  contact_id                integer,
  seller_id                 integer,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  status_id                 integer,
  workspace                 integer not null
);
create index order_trans_detail_ix1 on order_trans_detail (workspace, trans_date);

create table order_trans_factor (
  id                        integer identity(1,1) primary key,
  effect                    float default 0,
  amount                    float default 0,
  trans_id                  integer,
  factor_id                 integer
);

create table order_trans_source (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index order_trans_source_ix1 on order_trans_source (workspace, name);

create table order_trans_status (
  id                        integer identity(1,1) primary key,
  parent_id                 integer,
  name                      varchar(30) not null,
  ordering                  integer default 0,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0
);
create index order_trans_status_ix1 on order_trans_status (name);

create table order_trans_status_history (
  id                        integer identity(1,1) primary key,
  trans_time                datetime,
  trans_id                  integer not null,
  status_id                 integer not null,
  username                  varchar(20),
  description               varchar(150)
);

create table safe (
  id                        integer identity(1,1) primary key,
  name                      varchar(50) not null,
  exc_code                  varchar(3),
  responsible               varchar(30),
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index safe_ix1 on safe (workspace, name);

create table safe_expense (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index safe_expense_ix1 on safe_expense (workspace, name);

create table safe_trans (
  id                        integer identity(1,1) primary key,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  trans_no                  varchar(20),
  trans_type                varchar(6) not null,
  trans_dir                 smallint default 0,
  amount                    float default 0 not null,
  debt                      float default 0 not null,
  credit                    float default 0 not null,
  description               varchar(100),
  trans_year                smallint,
  trans_month               varchar(7),
  exc_code                  varchar(3),
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  safe_id                   integer not null,
  expense_id                integer,
  ref_module                varchar(10),
  ref_id                    integer,
  workspace                 integer not null,
  version                   integer default 0
);
create index safe_trans_ix1 on safe_trans (workspace, _right, trans_date);

create table safe_trans_source (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index safe_trans_source_ix1 on safe_trans_source (workspace, name);

create table sale_seller (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  prim_rate                 float not null,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index sale_seller_ix1 on sale_seller (workspace, name);

create table sale_campaign (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  start_date                date not null,
  end_date                  date not null,
  discount_rate1            float default 0,
  discount_rate2            float default 0,
  discount_rate3            float default 0,
  priority                  smallint default 1,
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
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index sale_campaign_ix1 on sale_campaign (workspace, name);

create table stock (
  id                        integer identity(1,1) primary key,
  code                      varchar(30) not null,
  name                      varchar(100) not null,
  exc_code                  varchar(3),
  provider_code             varchar(30),
  unit1                     varchar(6),
  unit2                     varchar(6),
  unit3                     varchar(6),
  unit2ratio                float default 0,
  unit3ratio                float default 0,
  buy_price                 float default 0,
  sell_price                float default 0,
  buy_tax                   float default 0,
  sell_tax                  float default 0,
  tax_rate2                 float default 0,
  tax_rate3                 float default 0,
  prim_rate                 float default 0,
  max_limit                 float default 0,
  min_limit                 float default 0,
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
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index stock_ix1 on stock (workspace, name);
create index stock_ix2 on stock (workspace, code);

create table stock_barcode (
  id                        integer identity(1,1) primary key,
  barcode                   varchar(128) not null,
  prefix                    varchar(30),
  suffix                    varchar(30),
  unit_no                   smallint default 1,
  is_primary                bit default 0,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  stock_id                  integer,
  workspace                 integer not null
);
create index stock_barcode_ix1 on stock_barcode (workspace, barcode);

create table stock_category (
  id                        integer identity(1,1) primary key,
  par1id                    integer,
  par2id                    integer,
  par3id                    integer,
  par4id                    integer,
  par5id                    integer,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  workspace                 integer not null,
  version                   integer default 0
);
create index stock_category_ix1 on stock_category (workspace, name);

create table stock_costing (
  id                        integer identity(1,1) primary key,
  name                      varchar(30),
  properties                varchar(100) not null,
  exec_date                 datetime,
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
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index stock_costing_ix1 on stock_costing (workspace, name);

create table stock_costing_detail (
  id                        integer identity(1,1) primary key,
  sell_date                 date not null,
  sell_quantity             float default 0,
  sell_cost_price           float default 0,
  sell_cost_amount          float default 0,
  buy_cost_price            float default 0,
  buy_cost_amount           float default 0,
  profit_loss_amount        float default 0,
  trans_year                smallint,
  trans_month               varchar(7),
  costing_id                integer,
  stock_id                  integer
);

create table stock_costing_inventory (
  id                        integer identity(1,1) primary key,
  _date                     date,
  input                     float default 0,
  remain                    float default 0,
  price                     float default 0,
  amount                    float default 0,
  costing_id                integer,
  stock_id                  integer,
  depot_id                  integer
);

create table stock_cost_factor (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  factor_type               varchar(8) not null,
  calc_type                 varchar(7) not null,
  effect_type               varchar(7) not null,
  effect                    float,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index stock_cost_factor_ix1 on stock_cost_factor (workspace, name);

create table stock_depot (
  id                        integer identity(1,1) primary key,
  name                      varchar(50) not null,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index stock_depot_ix1 on stock_depot (workspace, name);

create table stock_extra_fields (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  extra_fields_id           integer,
  workspace                 integer not null,
  version                   integer default 0
);
create index stock_extra_fields_ix1 on stock_extra_fields (workspace, extra_fields_id, name);

create table stock_price_list (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  start_date                date,
  end_date                  date,
  is_sell_price             bit default 1,
  effect_type               varchar(7) not null,
  effect_direction          varchar(8) not null,
  effect                    float default 0,
  description               varchar(50),
  provider_code             varchar(30),
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
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
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index stock_price_list_ix1 on stock_price_list (workspace, name);

create table stock_price_update (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  exec_date                 datetime,
  effect_type               varchar(7) not null,
  effect_direction          varchar(8) not null,
  effect                    float default 0,
  description               varchar(50),
  buy_price                 bit default 0,
  sell_price                bit default 0,
  provider_code             varchar(30),
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
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
  version                   integer default 0
);
create index stock_price_update_ix1 on stock_price_update (workspace, name);

create table stock_price_update_detail (
  id                        integer identity(1,1) primary key,
  price_update_id           integer,
  stock_id                  integer,
  buy_price                 float default 0,
  sell_price                float default 0,
);

create table stock_trans (
  id                        integer identity(1,1) primary key,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  is_completed              bit default 0,
  trans_date                date not null,
  real_date                 datetime,
  delivery_date             datetime,
  trans_no                  varchar(20),
  is_tax_include            bit default 1,
  rounding_digits           bit,
  total                     float default 0,
  discount_total            float default 0,
  subtotal                  float default 0,
  rounding_discount         float default 0,
  total_discount_rate       float default 0,
  tax_total                 float default 0,
  net_total                 float default 0,
  plus_factor_total         float default 0,
  minus_factor_total        float default 0,
  amount                    float default 0,
  description               varchar(100),
  trans_year                smallint,
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
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
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
  version                   integer default 0
);
create index stock_trans_ix1 on stock_trans (workspace, _right, trans_date);

create table stock_trans_detail (
  id                        integer identity(1,1) primary key,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  delivery_date             date,
  trans_type                varchar(6) not null,
  row_no                    integer,
  stock_id                  integer,
  name                      varchar(100),
  quantity                  float default 1,
  unit                      varchar(6),
  unit_ratio                float default 1,
  base_price                float default 0,
  price                     float default 0,
  tax_rate                  float default 0,
  tax_rate2                 float default 0,
  tax_rate3                 float default 0,
  discount_rate1            float default 0,
  discount_rate2            float default 0,
  discount_rate3            float default 0,
  amount                    float default 0,
  tax_amount                float default 0,
  discount_amount           float default 0,
  total                     float default 0,
  description               varchar(100),
  trans_year                smallint,
  trans_month               varchar(7),
  unit1                     varchar(6),
  unit2                     varchar(6),
  unit3                     varchar(6),
  unit2ratio                float default 0,
  unit3ratio                float default 0,
  exc_code                  varchar(3),
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  plus_factor_amount        float default 0,
  minus_factor_amount       float default 0,
  serial_no                 varchar(100),
  input                     float default 0,
  output                    float default 0,
  in_total                  float default 0,
  out_total                 float default 0,
  is_return                 bit default 0,
  ret_input                 float default 0,
  ret_output                float default 0,
  ret_in_total              float default 0,
  ret_out_total             float default 0,
  net_input                 float default 0,
  net_output                float default 0,
  net_in_total              float default 0,
  net_out_total             float default 0,
  has_cost_effect           bit default 1,
  trans_id                  integer,
  depot_id                  integer,
  contact_id                integer,
  seller_id                 integer,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  workspace                 integer not null
);
create index stock_trans_detail_ix1 on stock_trans_detail (workspace, trans_date);

create table stock_trans_factor (
  id                        integer identity(1,1) primary key,
  effect                    float default 0,
  quantity                  float default 0,
  amount                    float default 0,
  trans_id                  integer,
  factor_id                 integer
);

create table stock_trans_source (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  has_cost_effect           bit default 1,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index stock_trans_source_ix1 on stock_trans_source (workspace, name);

create table stock_trans_tax (
  id                        integer identity(1,1) primary key,
  tax_rate                  float default 0,
  basis                     float default 0,
  amount                    float default 0,
  trans_id                  integer
);

create table stock_trans_currency (
  id                        integer identity(1,1) primary key,
  currency                  varchar(3) default '',
  amount                    float default 0,
  trans_id                  integer
);

create table stock_unit (
  id                        integer identity(1,1) primary key,
  name                      varchar(7) not null,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index stock_unit_ix1 on stock_unit (workspace, name);

create table waybill_trans (
  id                        integer identity(1,1) primary key,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  is_completed              bit default 0,
  trans_date                date not null,
  real_date                 datetime,
  delivery_date             datetime,
  trans_no                  varchar(20),
  is_tax_include            bit default 1,
  rounding_digits           bit,
  total                     float default 0,
  discount_total            float default 0,
  subtotal                  float default 0,
  rounding_discount         float default 0,
  total_discount_rate       float default 0,
  tax_total                 float default 0,
  net_total                 float default 0,
  plus_factor_total         float default 0,
  minus_factor_total        float default 0,
  description               varchar(100),
  trans_year                smallint,
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
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  is_transfer               bit default 0,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
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
  version                   integer default 0
);
create index waybill_trans_ix1 on waybill_trans (workspace, _right, trans_date);

create table waybill_trans_detail (
  id                        integer identity(1,1) primary key,
  receipt_no                integer not null,
  _right                    varchar(50) not null,
  trans_date                date not null,
  delivery_date             date,
  trans_type                varchar(6) not null,
  row_no                    integer,
  stock_id                  integer,
  name                      varchar(100),
  quantity                  float default 1,
  unit                      varchar(6),
  unit_ratio                float default 1,
  base_price                float default 0,
  price                     float default 0,
  tax_rate                  float default 0,
  discount_rate1            float default 0,
  discount_rate2            float default 0,
  discount_rate3            float default 0,
  amount                    float default 0,
  tax_amount                float default 0,
  discount_amount           float default 0,
  total                     float default 0,
  description               varchar(100),
  trans_year                smallint,
  trans_month               varchar(7),
  unit1                     varchar(6),
  unit2                     varchar(6),
  unit3                     varchar(6),
  unit2ratio                float default 0,
  unit3ratio                float default 0,
  exc_code                  varchar(3),
  exc_rate                  float default 0,
  exc_equivalent            float default 0,
  plus_factor_amount        float default 0,
  minus_factor_amount       float default 0,
  input                     float default 0,
  output                    float default 0,
  in_total                  float default 0,
  out_total                 float default 0,
  net_input                 float default 0,
  net_output                float default 0,
  net_in_total              float default 0,
  net_out_total             float default 0,
  completed                 float default 0,
  cancelled                 float default 0,
  is_transfer               bit default 0,
  trans_id                  integer,
  depot_id                  integer,
  contact_id                integer,
  seller_id                 integer,
  trans_source_id           integer,
  trans_point_id            integer,
  private_code_id           integer,
  status_id                 integer,
  workspace                 integer not null
);
create index waybill_trans_detail_ix1 on waybill_trans_detail (workspace, trans_date);

create table waybill_trans_factor (
  id                        integer identity(1,1) primary key,
  effect                    float default 0,
  amount                    float default 0,
  trans_id                  integer,
  factor_id                 integer
);

create table waybill_trans_relation (
  id                        integer identity(1,1) primary key,
  rel_id                    integer not null,
  rel_right                 varchar(30) not null,
  rel_receipt_no            integer not null,
  trans_id                  integer
);

create table waybill_trans_source (
  id                        integer identity(1,1) primary key,
  name                      varchar(30) not null,
  suitable_right            varchar(30),
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0
);
create index waybill_trans_source_ix1 on waybill_trans_source (workspace, name);

create table waybill_trans_status (
  id                        integer identity(1,1) primary key,
  parent_id                 integer,
  name                      varchar(30) not null,
  ordering                  integer default 0,
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 boolean default true,
  workspace                 integer not null,
  version                   integer default 0
);
create index waybill_trans_status_ix1 on waybill_trans_status (name);

create table waybill_trans_status_history (
  id                        integer identity(1,1) primary key,
  trans_time                datetime,
  trans_id                  integer not null,
  status_id                 integer not null,
  username                  varchar(20),
  description               varchar(150)
);

-- Temp tables

create table temp_contact_aging (
  username                  varchar(20) not null,
  contact_name              varchar(100),
  receipt_no                integer,
  _right                    varchar(50) not null,
  trans_date                date not null,
  trans_no                  varchar(20),
  amount                    float default 0,
  paid                      float default 0,
  remain                    float default 0,
  exc_code                  varchar(3),
  description               varchar(100)
);
create index temp_contact_aging_ix1 on temp_contact_aging (username);
create index temp_contact_aging_ix2 on temp_contact_aging (contact_name);
create index temp_contact_aging_ix3 on temp_contact_aging (trans_date);

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

insert into admin_user (username, title, password_hash, is_admin, is_active) values ('super', 'super user', '81dc9bdb52d04dc20036dbd8313ed055', 1, 1);
insert into admin_user (username, title, password_hash, is_admin, is_active) values ('admin', 'administrator', '81dc9bdb52d04dc20036dbd8313ed055', 1, 1);

insert into admin_workspace (name, description) values ('seyhan', 'seyhan çalışma dönemi');

insert into contact_category (name, working_dir, debt_limit, credit_limit, workspace, insert_by, insert_at, is_active) values ('GENEL', null, 0, 0, 1, 'admin', GETDATE(), 1);
insert into contact_category (name, working_dir, debt_limit, credit_limit, workspace, insert_by, insert_at, is_active) values ('MÜŞTERI', 'Debt', 0, 0, 1, 'admin', GETDATE(), 1);
insert into contact_category (name, working_dir, debt_limit, credit_limit, workspace, insert_by, insert_at, is_active) values ('SATICI', 'Credit', 0, 0, 1, 'admin', GETDATE(), 1);
insert into contact_category (name, working_dir, debt_limit, credit_limit, workspace, insert_by, insert_at, is_active) values ('DAĞITICI', null, 0, 0, 1, 'admin', GETDATE(), 1);
insert into contact_category (name, working_dir, debt_limit, credit_limit, workspace, insert_by, insert_at, is_active) values ('BAYI', null, 0, 0, 1, 'admin', GETDATE(), 1);

insert into global_currency (code, name, insert_by, insert_at, is_active) values ('USD', 'ABD DOLARI', 'admin', GETDATE(), 1);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('EUR', 'EURO', 'admin', GETDATE(), 1);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('CNY', 'ÇİN YUANI', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('RUB', 'RUS RUBLESİ', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('SAR', 'SUUDİ ARABİSTAN RİYALİ', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('JPY', 'JAPON YENİ', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('GBP', 'İNGİLİZ STERLİNİ', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('IRR', 'İRAN RİYALİ', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('CAD', 'KANADA DOLARI', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('KWD', 'KUVEYT DİNARI', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('AUD', 'AVUSTRALYA DOLARI', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('DKK', 'DANİMARKA KRONU', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('CHF', 'İSVİÇRE FRANGI', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('SEK', 'İSVEÇ KRONU', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('NOK', 'NORVEÇ KRONU', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('BGN', 'BULGAR LEVASI', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('RON', 'RUMEN LEYİ', 'admin', GETDATE(), 0);
insert into global_currency (code, name, insert_by, insert_at, is_active) values ('PKR', 'PAKİSTAN RUPİSİ', 'admin', GETDATE(), 0);

insert into bank_trans_source (name, suitable_right, workspace, insert_by, insert_at, is_active) values ('BORÇ AÇILIŞ İŞLEMI', 'BANK_HESABA_PARA_GIRISI', 1, 'admin', GETDATE(), 1);
insert into bank_trans_source (name, suitable_right, workspace, insert_by, insert_at, is_active) values ('ALACAK AÇILIŞ İŞLEMI', 'BANK_HESAPTAN_PARA_CIKISI', 1, 'admin', GETDATE(), 1);
insert into bank_trans_source (name, suitable_right, workspace, insert_by, insert_at, is_active) values ('GELEN EFT', 'BANK_HESABA_PARA_GIRISI', 1, 'admin', GETDATE(), 1);
insert into bank_trans_source (name, suitable_right, workspace, insert_by, insert_at, is_active) values ('GÖNDERILEN EFT', 'BANK_HESAPTAN_PARA_CIKISI', 1, 'admin', GETDATE(), 1);
insert into bank_trans_source (name, suitable_right, workspace, insert_by, insert_at, is_active) values ('GELEN HAVALE', 'BANK_HESABA_PARA_GIRISI', 1, 'admin', GETDATE(), 1);
insert into bank_trans_source (name, suitable_right, workspace, insert_by, insert_at, is_active) values ('GÖNDERILEN HAVALE', 'BANK_HESAPTAN_PARA_CIKISI', 1, 'admin', GETDATE(), 1);
insert into bank_trans_source (name, suitable_right, workspace, insert_by, insert_at, is_active) values ('ELDEN YATIRILAN', 'BANK_HESABA_PARA_GIRISI', 1, 'admin', GETDATE(), 1);
insert into bank_trans_source (name, suitable_right, workspace, insert_by, insert_at, is_active) values ('ELDEN ÇEKILEN', 'BANK_HESAPTAN_PARA_CIKISI', 1, 'admin', GETDATE(), 1);

insert into contact_trans_source (name, suitable_right, workspace, insert_by, insert_at, is_active) values ('BORÇ AÇILIŞ İŞLEMI', 'CARI_BORC_DEKONTU', 1, 'admin', GETDATE(), 1);
insert into contact_trans_source (name, suitable_right, workspace, insert_by, insert_at, is_active) values ('ALACAK AÇILIŞ İŞLEMI', 'CARI_ALACAK_DEKONTU', 1, 'admin', GETDATE(), 1);
insert into safe_trans_source (name, suitable_right, workspace, insert_by, insert_at, is_active) values ('BORÇ AÇILIŞ İŞLEMI', 'KASA_MAHSUP_FISI', 1, 'admin', GETDATE(), 1);
insert into safe_trans_source (name, suitable_right, workspace, insert_by, insert_at, is_active) values ('ALACAK AÇILIŞ İŞLEMI', 'KASA_MAHSUP_FISI', 1, 'admin', GETDATE(), 1);
insert into stock_trans_source (name, suitable_right, has_cost_effect, workspace, insert_by, insert_at, is_active) values ('AÇILIŞ FIŞI', 'STOK_GIRIS_FISI', 1, 1, 'admin', GETDATE(), 1);
insert into invoice_trans_source (name, suitable_right, has_cost_effect, has_stock_effect, workspace, insert_by, insert_at, is_active) values ('HIZMET FATURASI', null, 0, 0, 1, 'admin', GETDATE(), 1);

insert into stock_unit (name, workspace, insert_by, insert_at, is_active) values ('ADET', 1, 'admin', GETDATE(), 1);
insert into stock_unit (name, workspace, insert_by, insert_at, is_active) values ('PAKET', 1, 'admin', GETDATE(), 1);
insert into stock_unit (name, workspace, insert_by, insert_at, is_active) values ('KOLI', 1, 'admin', GETDATE(), 1);
insert into stock_unit (name, workspace, insert_by, insert_at, is_active) values ('KG', 1, 'admin', GETDATE(), 0);
insert into stock_unit (name, workspace, insert_by, insert_at, is_active) values ('GR', 1, 'admin', GETDATE(), 0);

insert into safe (name, workspace, insert_by, insert_at, is_active) values ('MERKEZ KASA', 1, 'admin', GETDATE(), 1);
insert into stock_depot (name, workspace, insert_by, insert_at, is_active) values ('MERKEZ DEPO', 1, 'admin', GETDATE(), 1);
insert into global_trans_point (name, workspace, insert_by, insert_at) values ('GENEL', 1, 'admin', GETDATE());

insert into admin_document_target (name, is_local, target_type, view_type, path, is_compressed, description, is_active) values ('local_file', 1, 'FILE', 'PORTRAIT', '//opt/', 1, 'Yerel dosya', 1);
insert into admin_document_target (name, is_local, target_type, view_type, path, is_compressed, description, is_active) values ('local_printer', 1, 'DOT_MATRIX', 'PORTRAIT', 'LPT1:', 1, 'Yerel Nokta Vuruşlu yazıcı', 1);
insert into admin_document_target (name, is_local, target_type, view_type, path, is_compressed, description, is_active) values ('local_lazer', 1, 'LASER', 'PORTRAIT', '', 1, 'Yerel Lazer yazıcı', 1);
insert into admin_document_target (name, is_local, target_type, view_type, path, is_compressed, description, is_active) values ('remote_file', 0, 'FILE', 'PORTRAIT', '//opt/', 1, 'Uzak dosya', 0);
insert into admin_document_target (name, is_local, target_type, view_type, path, is_compressed, description, is_active) values ('remote_printer', 0, 'DOT_MATRIX', 'PORTRAIT', 'LPT1:', 1, 'Uzak Nokta Vuruşlu yazıcı', 0);
insert into admin_document_target (name, is_local, target_type, view_type, path, is_compressed, description, is_active) values ('remote_lazer', 0, 'LASER', 'PORTRAIT', '', 1, 'Uzak Lazer yazıcı', 0);

insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (0, 'contact', 'Gurup', 0, 1);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (1, 'contact', 'ek_alan1', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (2, 'contact', 'ek_alan2', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (3, 'contact', 'ek_alan3', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (4, 'contact', 'ek_alan4', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (5, 'contact', 'ek_alan5', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (6, 'contact', 'ek_alan6', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (7, 'contact', 'ek_alan7', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (8, 'contact', 'ek_alan8', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (9, 'contact', 'ek_alan9', 0, 0);

insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (0, 'stock', 'Gurup', 0, 1);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (1, 'stock', 'Marka', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (2, 'stock', 'Model', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (3, 'stock', 'Raf', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (4, 'stock', 'Renk', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (5, 'stock', 'Beden', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (6, 'stock', 'Boy', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (7, 'stock', 'ek_alan7', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (8, 'stock', 'ek_alan8', 0, 0);
insert into admin_extra_fields (idno, distinction, name, is_required, is_active) values (9, 'stock', 'ek_alan9', 0, 0);


-- db operations for novaposhta

create table novaposhta_cargo (
  id                        integer identity(1,1) primary key,
  name                      varchar(100) not null,
  responsible               varchar(30),
  phone1                    varchar(15),
  phone2                    varchar(15),
  address1                  varchar(150),
  address2                  varchar(150),
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  is_active                 bit default 1,
  workspace                 integer not null,
  version                   integer default 0,
);
create index novaposhta_cargo_ix1 on novaposhta_cargo_company (workspace, name);

create table novaposhta_cargo_trans (
  id                        integer identity(1,1) primary key,
  registration_no           varchar(30) not null,
  trans_date                date not null,
  cargo_value               float default 0,
  money                     float default 0,
  _return                   float default 0,
  total                     float default 0,
  description               varchar(100),
  cargo_id                  integer not null,
  trans_year                smallint,
  trans_month               varchar(7),
  insert_by                 varchar(20),
  insert_at                 datetime,
  update_by                 varchar(20),
  update_at                 datetime,
  workspace                 integer not null,
  version                   integer default 0,
  primary key (id)
) engine=innodb default charset=utf8;
create index novaposhta_cargo_trans_ix1 on novaposhta_cargo_trans (workspace, cargo_id, trans_date);


COMMIT TRANSACTION;
