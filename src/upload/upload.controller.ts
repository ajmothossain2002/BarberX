import { Controller, Get, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { UploadService } from './upload.service';

@Controller('upload')
@UseGuards(JwtAuthGuard)
export class UploadController {
    constructor(private uploadService: UploadService) { }

    @Get('signature')
    getSignature() {
        return this.uploadService.getUploadSignature();
    }
}
