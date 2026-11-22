-- 开发阶段关闭登录验证码，方便本地调试和页面排查。
-- 生产环境如需开启验证码，请将 sys.account.captchaEnabled 改回 true。

update sys_config
set config_value = 'false',
    update_by = 'admin',
    update_time = sysdate(),
    remark = '是否开启验证码功能（true开启，false关闭）；开发阶段默认关闭，生产环境建议开启'
where config_key = 'sys.account.captchaEnabled';

insert into sys_config (
    config_name,
    config_key,
    config_value,
    config_type,
    create_by,
    create_time,
    remark
)
select
    '账号自助-验证码开关',
    'sys.account.captchaEnabled',
    'false',
    'Y',
    'admin',
    sysdate(),
    '是否开启验证码功能（true开启，false关闭）；开发阶段默认关闭，生产环境建议开启'
from dual
where not exists (
    select 1
    from sys_config
    where config_key = 'sys.account.captchaEnabled'
);
